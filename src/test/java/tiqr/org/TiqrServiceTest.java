package tiqr.org;

import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;
import tiqr.org.model.*;
import tiqr.org.push.APNSConfiguration;
import tiqr.org.push.GCMConfiguration;
import tiqr.org.repo.AuthenticationRepository;
import tiqr.org.repo.EnrollmentRepository;
import tiqr.org.repo.RegistrationRepository;
import tiqr.org.secure.Challenge;
import tiqr.org.secure.OCRA;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class TiqrServiceTest {

    private final EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
    private final RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
    private final AuthenticationRepository authenticationRepository = mock(AuthenticationRepository.class);

    private final String sharedSecret = Challenge.generateNonce();

    private final DefaultTiqrService tiqrService = new DefaultTiqrService(
            enrollmentRepository,
            registrationRepository,
            authenticationRepository,
            new Service(
                    "test",
                    "test",
                    "1.0",
                    "http://localhost/logo",
                    "http://localhost/info",
                    "http://localhost/authention",
                    true,
                    "http://localhost/enroll"
            ),
            "secret",
            new APNSConfiguration(
                    "localhost",
                    8099,
                    "classpath:/token-auth-private-key.p8",
                    "classpath:/ca.pem",
                    "topic",
                    "teamId",
                    "keyId"),
            new GCMConfiguration(
                    "classpath:/test-firebase-adminsdk.json",
                    UUID.randomUUID().toString()));

    @Test
    void enrollmentScenario() throws TiqrException {
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArguments()[0]);
        String userId = "user-id";
        Enrollment enrollment = tiqrService.startEnrollment(userId, "John Doe");

        assertEquals(EnrollmentStatus.INITIALIZED, enrollment.getStatus());
        assertNotNull(enrollment.getKey());

        when(enrollmentRepository.findEnrollmentByKey(enrollment.getKey())).thenReturn(Optional.of(enrollment));
        MetaData metaData = tiqrService.getMetaData(enrollment.getKey());
        String enrollmentSecret = UriComponentsBuilder.fromUriString(metaData.getService().getEnrollmentUrl()).build()
                .getQueryParams()
                .getFirst("enrollment_secret");
        assertNotNull(enrollmentSecret);

        assertEquals(EnrollmentStatus.RETRIEVED, tiqrService.enrollmentStatus(enrollment.getKey()).getStatus());
        assertEquals(metaData.getIdentity().getIdentifier(), enrollment.getRegistrationId());

        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArguments()[0]);
        when(enrollmentRepository.findEnrollmentByEnrollmentSecret(enrollmentSecret)).thenReturn(Optional.of(enrollment));

        Registration registration = getRegistration(enrollmentSecret);
        Registration result = tiqrService.enrollData(registration);
        assertEquals(metaData.getIdentity().getIdentifier(), registration.getId());

        SecretCipher cipher = new SecretCipher("secret");
        assertEquals(result.getSecret(), cipher.encrypt(sharedSecret));

        when(registrationRepository.findRegistrationByUserId(userId))
                .thenReturn(Optional.of(registration));
        assertThrows(TiqrException.class, () ->
                tiqrService.startAuthentication(userId, "John Doe", "https://eduid.nl/tiqrauth", false));

        tiqrService.finishRegistration(userId);
        assertEquals(RegistrationStatus.FINALIZED, registration.getStatus());

        when(authenticationRepository.save(any(Authentication.class))).thenAnswer(i -> i.getArguments()[0]);
        Authentication authentication = tiqrService.startAuthentication(userId, "John Doe", "https://eduid.nl/tiqrauth", false);

        when(authenticationRepository.findAuthenticationBySessionKey(authentication.getSessionKey()))
                .thenReturn(Optional.of(authentication));
        assertEquals(AuthenticationStatus.PENDING, tiqrService.authenticationStatus(authentication.getSessionKey()).getStatus());

        when(registrationRepository.findRegistrationByUserId(authentication.getUserID()))
                .thenReturn(Optional.of(registration));
        AuthenticationData authenticationData = new AuthenticationData(
                authentication.getSessionKey(),
                userId,
                OCRA.generateOCRA(sharedSecret, authentication.getChallenge(), authentication.getSessionKey()),
                "nl",
                "login",
                "APNS",
                "01234567890"
        );
        tiqrService.postAuthentication(authenticationData);
        assertEquals(AuthenticationStatus.SUCCESS, tiqrService.authenticationStatus(authentication.getSessionKey()).getStatus());

        //Mimic the scenario where the user manually enters the TOTP code
        authentication.setStatus(AuthenticationStatus.PENDING);
        when(authenticationRepository.findAuthenticationBySessionKey(authentication.getSessionKey()))
                .thenReturn(Optional.of(authentication));
        tiqrService.postAuthentication(new AuthenticationData(authenticationData.getSessionKey(), authenticationData.getResponse()));
        assertEquals(AuthenticationStatus.SUCCESS, tiqrService.authenticationStatus(authentication.getSessionKey()).getStatus());
    }

    @Test
    void ensureInitializedStatus() {
        Enrollment enrollment = new Enrollment("key", "user-id", "display-name", EnrollmentStatus.RETRIEVED);
        when(enrollmentRepository.findEnrollmentByKey(enrollment.getKey())).thenReturn(Optional.of(enrollment));

        assertThrows(TiqrException.class, () -> tiqrService.getMetaData(enrollment.getKey()));
    }

    @Test
    void ensureRegistrationIsValid() {
        Registration registration = new Registration();
        registration.setEnrollmentSecret("secret");
        Enrollment enrollment = new Enrollment("key", "user-id", "display-name", EnrollmentStatus.RETRIEVED);
        when(enrollmentRepository.findEnrollmentByEnrollmentSecret(registration.getEnrollmentSecret())).thenReturn(Optional.of(enrollment));

        assertThrows(IllegalArgumentException.class, () -> tiqrService.enrollData(registration));
    }

    @Test
    void ensureRetrievedStatus() {
        Enrollment enrollment = new Enrollment("key", "user-id", "display-name", EnrollmentStatus.PROCESSED);
        when(enrollmentRepository.findEnrollmentByEnrollmentSecret(enrollment.getEnrollmentSecret())).thenReturn(Optional.of(enrollment));

        Registration registration = getRegistration(enrollment.getEnrollmentSecret());
        assertThrows(TiqrException.class, () -> tiqrService.enrollData(registration));
    }

    @Test
    void ensurePendingStatus() {
        Authentication authentication = new Authentication(
                "user-id",
                "John Doe",
                Challenge.generateSessionKey(),
                Challenge.generateQH10Challenge(),
                "https://eduid.nl/tiqrauth",
                AuthenticationStatus.SUCCESS);
        when(authenticationRepository.findAuthenticationBySessionKey(authentication.getSessionKey())).thenReturn(Optional.of(authentication));

        assertThrows(TiqrException.class, () -> tiqrService.postAuthentication(
                new AuthenticationData(
                        authentication.getSessionKey(),
                        "N/A",
                        authentication.getChallenge(),
                        "en",
                        "login",
                        "APNS",
                        "notificationAddress")));
    }

    @Test
    void suspendAuthentication() throws TiqrException {
        String sessionKey = Challenge.generateSessionKey();
        Authentication authentication = new Authentication(
                "user-id",
                "John Doe",
                sessionKey,
                Challenge.generateQH10Challenge(),
                "https://eduid.nl/tiqrauth",
                AuthenticationStatus.PENDING);
        when(authenticationRepository.findAuthenticationBySessionKey(sessionKey)).thenReturn(Optional.of(authentication));
        authentication = tiqrService.suspendAuthentication(sessionKey);
        assertEquals(AuthenticationStatus.SUSPENDED, authentication.getStatus());
    }

    @Test
    void enrollmentScenarioExistingRegistration() {
        Registration registration = new Registration();
        registration.setStatus(RegistrationStatus.FINALIZED);
        when(registrationRepository.findRegistrationByUserId(anyString())).thenReturn(Optional.of(registration));
        String userId = "user-id";
        assertThrows(TiqrException.class, () -> tiqrService.startEnrollment(userId, "John Doe"));
    }

    @Test
    void enrollmentScenarioExistingInitializedRegistration() throws TiqrException {
        Registration registration = new Registration();
        registration.setStatus(RegistrationStatus.INITIALIZED);
        when(registrationRepository.findRegistrationByUserId(anyString())).thenReturn(Optional.of(registration));
        String userId = "user-id";
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArguments()[0]);
        Enrollment enrollment = tiqrService.startEnrollment(userId, "John Doe");

        assertEquals(EnrollmentStatus.INITIALIZED, enrollment.getStatus());
        assertNotNull(enrollment.getKey());

    }

    private Registration getRegistration(String enrollmentSecret) {
        Registration registration = new Registration();
        registration.setUserId("user-id");
        registration.setSecret(sharedSecret);
        registration.setEnrollmentSecret(enrollmentSecret);
        registration.setLanguage("en");
        registration.setNotificationType("APNS");
        registration.setNotificationAddress("1234567890");
        registration.setOperation("register");
        return registration;
    }


}