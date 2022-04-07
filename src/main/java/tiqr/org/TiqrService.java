package tiqr.org;

import tiqr.org.model.*;
import tiqr.org.repo.AuthenticationRepository;
import tiqr.org.repo.EnrollmentRepository;
import tiqr.org.repo.RegistrationRepository;
import tiqr.org.secure.Challenge;
import tiqr.org.secure.SecretCipher;

import java.time.Instant;
import java.util.Optional;

public class TiqrService {

    private final EnrollmentRepository enrollmentRepository;
    private final RegistrationRepository registrationRepository;
    private final AuthenticationRepository authenticationRepository;

    private final Service service;
    private final SecretCipher secretCipher;

    public TiqrService(EnrollmentRepository enrollmentRepository,
                       RegistrationRepository registrationRepository,
                       AuthenticationRepository authenticationRepository,
                       Service service,
                       String secret) {
        this.enrollmentRepository = enrollmentRepository;
        this.registrationRepository = registrationRepository;
        this.authenticationRepository = authenticationRepository;
        this.service = service;
        this.secretCipher = new SecretCipher(secret);
    }

    public Enrollment startEnrollment(String userID, String userDisplayName) {
        Enrollment enrollment = new Enrollment(Challenge.generateNonce(), userID, userDisplayName, EnrollmentStatus.INITIALIZED);
        return enrollmentRepository.save(enrollment);
    }

    public MetaData getMetaData(String enrollmentKey) {
        Enrollment enrollment = enrollmentRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(IllegalArgumentException::new);

        if (!enrollment.getStatus().equals(EnrollmentStatus.INITIALIZED)) {
            throw new IllegalArgumentException("Metadata can only be retrieved when the status is INITIALIZED. Current status is " + enrollment.getStatus());
        }

        String enrollmentSecret = Challenge.generateNonce();
        enrollment.setEnrollmentSecret(enrollmentSecret);
        enrollment.update(EnrollmentStatus.RETRIEVED);

        enrollmentRepository.save(enrollment);
        return new MetaData(Service.addEnrollmentSecret(this.service, enrollmentSecret), new Identity(enrollment));
    }

    public Registration enrollData(Registration registration) {
        Enrollment enrollment = enrollmentRepository.findEnrollmentByEnrollmentSecret(registration.getEnrollmentSecret())
                .orElseThrow(IllegalArgumentException::new);

        if (!enrollment.getStatus().equals(EnrollmentStatus.RETRIEVED)) {
            throw new IllegalArgumentException("Enrollment can only be called when the status is RETRIEVED. Current status is " + enrollment.getStatus());
        }

        registration.setUserId(enrollment.getUserID());
        registration.setUserDisplayName(enrollment.getUserDisplayName());

        registration.validateForInitialEnrollment();

        registration.setSecret(secretCipher.encrypt(registration.getSecret()));
        Instant now = Instant.now();
        registration.setCreated(now);
        registration.setUpdated(now);

        Registration savedRegistration = registrationRepository.save(registration);

        enrollment.update(EnrollmentStatus.PROCESSED);
        enrollmentRepository.save(enrollment);

        return savedRegistration;
    }

    public Enrollment enrollmentStatus(String enrollmentKey) {
        return enrollmentRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(IllegalArgumentException::new);
    }

    public Authentication startAuthentication(String userId, String userDisplayName, boolean sendPushNotification) {
        Authentication authentication = new Authentication(
                userId,
                userDisplayName,
                Challenge.generateSessionKey(),
                Challenge.generateQH10Challenge(),
                AuthenticationStatus.PENDING);
        Authentication savedAuthentication = authenticationRepository.save(authentication);
        if (sendPushNotification) {
            //TODO
        }
        return savedAuthentication;
    }

    public void postAuthentication(AuthenticationData authenticationData) {
        Authentication authentication = authenticationRepository.findAuthenticationBySessionKey(authenticationData.getSessionKey())
                .orElseThrow(IllegalArgumentException::new);

        if (!authentication.getStatus().equals(AuthenticationStatus.PENDING)) {
            throw new IllegalArgumentException("Authentication can only be called when the status is PENDING. Current status is " + authentication.getStatus());
        }

        Registration registration = registrationRepository.findRegistrationByUserId(authentication.getUserID()).orElseThrow(IllegalArgumentException::new);
        String decryptedSecret = secretCipher.decrypt(registration.getSecret());
        Challenge.verifyOcra(decryptedSecret, authentication.getChallenge(), authentication.getSessionKey(), authenticationData.getResponse());

        registration.setNotificationAddress(authenticationData.getNotificationAddress());
        registration.setUpdated(Instant.now());

        registrationRepository.save(registration);

        authentication.update(AuthenticationStatus.SUCCESS);
        authenticationRepository.save(authentication);
    }

    public Authentication authenticationStatus(String sessionKey) {
        return authenticationRepository.findAuthenticationBySessionKey(sessionKey).orElseThrow(IllegalArgumentException::new);
    }


}
