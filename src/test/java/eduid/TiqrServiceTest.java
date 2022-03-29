package eduid;

import eduid.model.*;
import eduid.repo.EnrollmentRepository;
import eduid.repo.RegistrationRepository;
import eduid.secure.SecretCipher;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class TiqrServiceTest {

    private final EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);

    private final RegistrationRepository registrationRepository = mock(RegistrationRepository.class);

    private final TiqrService tiqrService = new TiqrService(enrollmentRepository, registrationRepository, new Service(
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

        assertEquals(EnrollmentStatus.INITIALIZED.getValue(), enrollment.getStatus().getValue());
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

    private Registration getRegistration(String enrollmentSecret) {
        return new Registration("user-id", "sharedSecret", enrollmentSecret, "nl", "APNS", "123456", "2", "register");
    }


}