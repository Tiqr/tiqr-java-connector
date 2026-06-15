package tiqr.org.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationTest {

    @Test
    void validateForInitialEnrollment() {
        Registration registration = new Registration();
        registration.setEnrollmentSecret("secret");
        registration.setStatus(RegistrationStatus.INITIALIZED);
        registration.setUserId("userId");
        registration.setNotificationAddress("address");
        registration.validateForInitialEnrollment();
    }
}