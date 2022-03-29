package eduid;

import eduid.model.*;
import eduid.repo.TiqrRepository;
import eduid.secure.Challenge;
import eduid.secure.SecretCipher;

public class TiqrService {

    private final TiqrRepository tiqrRepository;
    private final Service service;
    private final SecretCipher secretCipher;

    public TiqrService(TiqrRepository tiqrRepository, Service service, String secret) {
        this.tiqrRepository = tiqrRepository;
        this.service = service;
        this.secretCipher = new SecretCipher(secret);
    }

    Enrollment startEnrollment(String userID, String userDisplayName) {
        Enrollment enrollment = new Enrollment(Challenge.generateNonce(), userID, userDisplayName, EnrollmentStatus.INITIALIZED);
        return tiqrRepository.save(enrollment);
    }

    MetaData getMetaData(String enrollmentKey) {
        Enrollment enrollment = tiqrRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(IllegalArgumentException::new);

        if (!enrollment.getStatus().equals(EnrollmentStatus.INITIALIZED)) {
            throw new IllegalArgumentException("Metadata can only be retrieved when the status is INITIALIZED. Current status is " + enrollment.getStatus());
        }

        String enrollmentSecret = Challenge.generateNonce();
        enrollment.setEnrollmentSecret(enrollmentSecret);
        enrollment.update(EnrollmentStatus.RETRIEVED);

        tiqrRepository.save(enrollment);
        return new MetaData(Service.addEnrollmentSecret(this.service, enrollmentSecret), new Identity(enrollment));
    }

    void enrollData(Registration registration) {
        Enrollment enrollment = tiqrRepository.findEnrollmentByEnrollmentSecret(registration.getEnrollmentSecret())
                .orElseThrow(IllegalArgumentException::new);

        if (!enrollment.getStatus().equals(EnrollmentStatus.RETRIEVED)) {
            throw new IllegalArgumentException("Enrollment can only be called when the status is RETRIEVED. Current status is " + enrollment.getStatus());
        }

        enrollment.update(EnrollmentStatus.PROCESSED);
        tiqrRepository.save(enrollment);

        registration.setSecret(secretCipher.encrypt(registration.getSecret()));
        tiqrRepository.save(registration);
    }

    EnrollmentStatus enrollmentStatus(String enrollmentKey) {
        return tiqrRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(IllegalArgumentException::new).getStatus();
    }

}
