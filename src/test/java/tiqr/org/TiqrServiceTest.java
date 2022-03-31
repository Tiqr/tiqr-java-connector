package tiqr.org;

import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;
import tiqr.org.model.*;
import tiqr.org.repo.AuthenticationRepository;
import tiqr.org.repo.EnrollmentRepository;
import tiqr.org.repo.RegistrationRepository;
import tiqr.org.secure.OCRA;
import tiqr.org.secure.SecretCipher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class TiqrServiceTest {

    private final EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
    private final RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
    private final AuthenticationRepository authenticationRepository = mock(AuthenticationRepository.class);

    private final TiqrService tiqrService = new TiqrService(
            enrollmentRepository,
            registrationRepository,
            authenticationRepository,
            new Service(
                    "test",
                    "test",
                    "http://localhost/logo",
                    "http://localhost/info",
                    "http://localhost/authention",
                    "http://localhost/enroll"
            ), "secret");

    @Test
    void enrollmentScenario() {
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArguments()[0]);
        Enrollment enrollment = tiqrService.startEnrollment("user-id", "John Doe");

        assertEquals(EnrollmentStatus.INITIALIZED, enrollment.getStatus());
        assertNotNull(enrollment.getKey());

        when(enrollmentRepository.findEnrollmentByKey(enrollment.getKey())).thenReturn(Optional.of(enrollment));
        MetaData metaData = tiqrService.getMetaData(enrollment.getKey());
        String enrollmentSecret = UriComponentsBuilder.fromUriString(metaData.getService().getEnrollmentUrl()).build()
                .getQueryParams()
                .getFirst("enrollment_secret");
        assertNotNull(enrollmentSecret);

        assertEquals(EnrollmentStatus.RETRIEVED, tiqrService.enrollmentStatus(enrollment.getKey()));

        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArguments()[0]);
        when(enrollmentRepository.findEnrollmentByEnrollmentSecret(enrollmentSecret)).thenReturn(Optional.of(enrollment));

        Registration registration = getRegistration(enrollmentSecret);
        Registration result = tiqrService.enrollData(registration);

        SecretCipher cipher = new SecretCipher("secret");
        assertEquals(result.getSecret(), cipher.encrypt("sharedSecret"));

        when(authenticationRepository.save(any(Authentication.class))).thenAnswer(i -> i.getArguments()[0]);
        Authentication authentication = tiqrService.startAuthentication("user-id");

        when(authenticationRepository.findAuthenticationBySessionKey(authentication.getSessionKey()))
                .thenReturn(Optional.of(authentication));
        assertEquals(AuthenticationStatus.PENDING, tiqrService.authenticationStatus(authentication.getSessionKey()));

        when(registrationRepository.findRegistrationByUserId(authentication.getUserID()))
                .thenReturn(Optional.of(registration));
        AuthenticationData authenticationData = new AuthenticationData(
                authentication.getSessionKey(),
                OCRA.generateOCRA("sharedSecret", authentication.getChallenge(), authentication.getSessionKey()),
                "987654321"
        );
        tiqrService.postAuthentication(authenticationData);
        assertEquals(AuthenticationStatus.SUCCESS, tiqrService.authenticationStatus(authentication.getSessionKey()));
    }

    @Test
    void ensureInitializedStatus() {
        Enrollment enrollment = new Enrollment("key", "user-id", "display-name", EnrollmentStatus.RETRIEVED);
        when(enrollmentRepository.findEnrollmentByKey(enrollment.getKey())).thenReturn(Optional.of(enrollment));

        assertThrows(IllegalArgumentException.class, () -> tiqrService.getMetaData(enrollment.getKey()));
    }

    @Test
    void ensureRetrievedStatus() {
        Enrollment enrollment = new Enrollment("key", "user-id", "display-name", EnrollmentStatus.PROCESSED);
        when(enrollmentRepository.findEnrollmentByEnrollmentSecret(enrollment.getEnrollmentSecret())).thenReturn(Optional.of(enrollment));

        Registration registration = getRegistration(enrollment.getEnrollmentSecret());
        assertThrows(IllegalArgumentException.class, () -> tiqrService.enrollData(registration));
    }

    @Test
    void QRCodeGenerator() {
        Authentication authentication = new Authentication("user-id", "session-key", "challenge", AuthenticationStatus.SUCCESS);
        when(authenticationRepository.findAuthenticationBySessionKey(authentication.getSessionKey())).thenReturn(Optional.of(authentication));

        assertThrows(IllegalArgumentException.class, () -> tiqrService.postAuthentication(
                new AuthenticationData(authentication.getSessionKey(), authentication.getChallenge(), "notificationAddress")));
    }

    private Registration getRegistration(String enrollmentSecret) {
        return new Registration("user-id", "sharedSecret", enrollmentSecret, "nl", "APNS", "123456", "2", "register");
    }


}