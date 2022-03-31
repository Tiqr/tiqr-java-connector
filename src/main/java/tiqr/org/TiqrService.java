package tiqr.org;

import tiqr.org.model.*;
import tiqr.org.repo.AuthenticationRepository;
import tiqr.org.repo.EnrollmentRepository;
import tiqr.org.repo.RegistrationRepository;
import tiqr.org.secure.Challenge;
import tiqr.org.secure.SecretCipher;

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

        enrollment.update(EnrollmentStatus.PROCESSED);
        enrollmentRepository.save(enrollment);

        registration.setSecret(secretCipher.encrypt(registration.getSecret()));
        return registrationRepository.save(registration);
    }

    public EnrollmentStatus enrollmentStatus(String enrollmentKey) {
        return enrollmentRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(IllegalArgumentException::new).getStatus();
    }

    public Authentication startAuthentication(String userId) {
        Authentication authentication = new Authentication(
                userId,
                Challenge.generateNonce(),
                Challenge.generateQN08Challenge(),
                AuthenticationStatus.PENDING);
        return authenticationRepository.save(authentication);
    }

    public void postAuthentication(AuthenticationData authenticationData) {
        Authentication authentication = authenticationRepository.findAuthenticationBySessionKey(authenticationData.getSessionKey()).orElseThrow(IllegalArgumentException::new);
        if (!authentication.getStatus().equals(AuthenticationStatus.PENDING)) {
            throw new IllegalArgumentException("Authentication can only be called when the status is PENDING. Current status is " + authentication.getStatus());
        }

        Registration registration = registrationRepository.findRegistrationByUserId(authentication.getUserID()).orElseThrow(IllegalArgumentException::new);
        String decryptedSecret = secretCipher.decrypt(registration.getSecret());
        Challenge.verifyOcra(decryptedSecret, authentication.getChallenge(), authentication.getSessionKey(), authenticationData.getResponse());

        registration.setNotificationAddress(authenticationData.getNotificationAddress());

        registrationRepository.save(registration);

        authentication.update(AuthenticationStatus.SUCCESS);
        authenticationRepository.save(authentication);
    }

    public AuthenticationStatus authenticationStatus(String sessionKey) {
        return authenticationRepository.findAuthenticationBySessionKey(sessionKey).orElseThrow(IllegalArgumentException::new).getStatus();
    }


}
