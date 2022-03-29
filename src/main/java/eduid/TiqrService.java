package eduid;

import eduid.model.*;
import eduid.repo.EnrollmentRepository;
import eduid.repo.RegistrationRepository;
import eduid.secure.Challenge;
import eduid.secure.SecretCipher;

public class TiqrService {

    private final EnrollmentRepository enrollmentRepository;
    private final RegistrationRepository registrationRepository;

    private final Service service;
    private final SecretCipher secretCipher;

    public TiqrService(EnrollmentRepository enrollmentRepository,
                       RegistrationRepository registrationRepository,
                       Service service,
                       String secret) {
        this.enrollmentRepository = enrollmentRepository;
        this.registrationRepository = registrationRepository;
        this.service = service;
        this.secretCipher = new SecretCipher(secret);
    }

    Enrollment startEnrollment(String userID, String userDisplayName) {
        Enrollment enrollment = new Enrollment(Challenge.generateNonce(), userID, userDisplayName, EnrollmentStatus.INITIALIZED);
        return enrollmentRepository.save(enrollment);
    }

    MetaData getMetaData(String enrollmentKey) {
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

    Registration enrollData(Registration registration) {
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

    EnrollmentStatus enrollmentStatus(String enrollmentKey) {
        return enrollmentRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(IllegalArgumentException::new).getStatus();
    }

}
