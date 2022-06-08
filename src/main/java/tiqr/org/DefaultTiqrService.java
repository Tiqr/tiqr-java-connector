package tiqr.org;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import tiqr.org.model.*;
import tiqr.org.push.APNSConfiguration;
import tiqr.org.push.GCMConfiguration;
import tiqr.org.push.NotificationGateway;
import tiqr.org.repo.AuthenticationRepository;
import tiqr.org.repo.EnrollmentRepository;
import tiqr.org.repo.RegistrationRepository;
import tiqr.org.secure.Challenge;
import tiqr.org.secure.SecretCipher;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.Instant;

public class DefaultTiqrService implements TiqrService {

    private static final Log LOG = LogFactory.getLog(DefaultTiqrService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final RegistrationRepository registrationRepository;
    private final AuthenticationRepository authenticationRepository;

    private final Service service;
    private final SecretCipher secretCipher;
    private final NotificationGateway notificationGateway;

    public DefaultTiqrService(EnrollmentRepository enrollmentRepository,
                              RegistrationRepository registrationRepository,
                              AuthenticationRepository authenticationRepository,
                              Service service,
                              String secret,
                              APNSConfiguration apnsConfiguration,
                              GCMConfiguration gcmConfiguration) {
        this.enrollmentRepository = enrollmentRepository;
        this.registrationRepository = registrationRepository;
        this.authenticationRepository = authenticationRepository;
        this.service = service;
        this.secretCipher = new SecretCipher(secret);
        this.notificationGateway = new NotificationGateway(apnsConfiguration, gcmConfiguration);
    }

    @Override
    public Enrollment startEnrollment(String userID, String userDisplayName) {
        enrollmentRepository.deleteByUserID(userID);
        registrationRepository.deleteByUserId(userID);

        LOG.debug("Starting enrollment for user " + userID);

        Enrollment enrollment = new Enrollment(Challenge.generateNonce(), userID, userDisplayName, EnrollmentStatus.INITIALIZED);
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public MetaData getMetaData(String enrollmentKey) {
        Enrollment enrollment = enrollmentRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(IllegalArgumentException::new);

        if (!enrollment.getStatus().equals(EnrollmentStatus.INITIALIZED)) {
            throw new IllegalArgumentException("Metadata can only be retrieved when the status is INITIALIZED. Current status is " + enrollment.getStatus());
        }

        String enrollmentSecret = Challenge.generateNonce();
        enrollment.setEnrollmentSecret(enrollmentSecret);
        enrollment.update(EnrollmentStatus.RETRIEVED);

        LOG.debug("Get metadata for enrollment for user " + enrollment.getUserID());

        enrollmentRepository.save(enrollment);
        return new MetaData(Service.addEnrollmentSecret(this.service, enrollmentSecret), new Identity(enrollment));
    }

    @Override
    public Registration enrollData(Registration registration) {
        Enrollment enrollment = enrollmentRepository.findEnrollmentByEnrollmentSecret(registration.getEnrollmentSecret())
                .orElseThrow(IllegalArgumentException::new);

        if (!enrollment.getStatus().equals(EnrollmentStatus.RETRIEVED)) {
            throw new IllegalArgumentException("Enrollment can only be called when the status is RETRIEVED. Current status is " + enrollment.getStatus());
        }

        registration.setUserId(enrollment.getUserID());
        registration.setUserDisplayName(enrollment.getUserDisplayName());
        registration.setStatus(RegistrationStatus.INITIALIZED);

        registration.validateForInitialEnrollment();

        registration.setSecret(secretCipher.encrypt(registration.getSecret()));
        Instant now = Instant.now();
        registration.setCreated(now);
        registration.setUpdated(now);

        Registration savedRegistration = registrationRepository.save(registration);

        enrollment.update(EnrollmentStatus.PROCESSED);
        enrollmentRepository.save(enrollment);

        LOG.debug("Preliminary registration for " + enrollment.getUserID());

        return savedRegistration;
    }

    @Override
    public Registration finishRegistration(String userId) {
        Registration registration = registrationRepository.findRegistrationByUserId(userId).orElseThrow(IllegalArgumentException::new);
        registration.setStatus(RegistrationStatus.FINALIZED);

        LOG.debug("Finished registration for " + userId);

        return registrationRepository.save(registration);
    }

    @Override
    public Enrollment enrollmentStatus(String enrollmentKey) {
        return enrollmentRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public Authentication startAuthentication(String userId, String userDisplayName, String eduIdAppBaseUrl, boolean sendPushNotification) {
        Registration registration = registrationRepository.findRegistrationByUserId(userId).orElseThrow(IllegalArgumentException::new);

        if (!RegistrationStatus.FINALIZED.equals(registration.getStatus())) {
            throw new IllegalArgumentException("Registration is not FINALIZED, but " + registration.getStatus());
        }
        String sessionKey = Challenge.generateSessionKey();
        String challenge = Challenge.generateQH10Challenge();
        String authenticationUrl = String.format("%s/tiqrauth?u=%s&s=%s&q=%s&i=%s&v=%s",
                eduIdAppBaseUrl,
                encode(userId),
                encode(sessionKey),
                encode(challenge),
                encode(this.service.getIdentifier()),
                encode(this.service.getVersion()));

        Authentication authentication = new Authentication(
                userId,
                userDisplayName,
                sessionKey,
                challenge,
                authenticationUrl,
                AuthenticationStatus.PENDING);

        Authentication savedAuthentication = authenticationRepository.save(authentication);
        if (sendPushNotification) {
            notificationGateway.push(registration, authenticationUrl);
        }

        LOG.debug("Started authentication for " + userId);

        return savedAuthentication;
    }

    @Override
    public void postAuthentication(AuthenticationData authenticationData) {
        Authentication authentication = authenticationRepository.findAuthenticationBySessionKey(authenticationData.getSessionKey())
                .orElseThrow(IllegalArgumentException::new);

        if (!authentication.getStatus().equals(AuthenticationStatus.PENDING)) {
            throw new IllegalArgumentException("Authentication can only be called when the status is PENDING. Current status is " + authentication.getStatus());
        }

        Registration registration = registrationRepository.findRegistrationByUserId(authentication.getUserID()).orElseThrow(IllegalArgumentException::new);
        String decryptedSecret = secretCipher.decrypt(registration.getSecret());
        Challenge.verifyOcra(decryptedSecret, authentication.getChallenge(), authentication.getSessionKey(), authenticationData.getResponse());


        String notificationAddress = authenticationData.getNotificationAddress();
        if (StringUtils.hasText(notificationAddress)) {
            registration.setNotificationAddress(notificationAddress);
        }
        registration.setUpdated(Instant.now());
        registrationRepository.save(registration);

        authentication.update(AuthenticationStatus.SUCCESS);
        authenticationRepository.save(authentication);

        LOG.debug("Finished authentication for " + authentication.getUserID());
    }

    @Override
    public Authentication authenticationStatus(String sessionKey) {
        return authenticationRepository.findAuthenticationBySessionKey(sessionKey).orElseThrow(IllegalArgumentException::new);
    }

    private String encode(String s) {
        return URLEncoder.encode(s, Charset.defaultCharset());
    }

}
