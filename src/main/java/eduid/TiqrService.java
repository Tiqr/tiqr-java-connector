package eduid;

import eduid.model.*;
import eduid.repo.TiqrRepository;
import eduid.secure.Challenge;

public class TiqrService {

    private final TiqrRepository tiqrRepository;
    private final Service service;

    public TiqrService(TiqrRepository tiqrRepository, Service service) {
        this.tiqrRepository = tiqrRepository;
        this.service = service;
    }

    Enrollment startEnrollment(String userID, String userDisplayName) {
        Enrollment enrollment = new Enrollment(Challenge.getChallenge(), userID, userDisplayName, EnrollmentStatus.INITIALIZED);
        return tiqrRepository.save(enrollment);
    }

    MetaData getMetaData(String enrollmentKey) {
        Enrollment enrollment = tiqrRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(IllegalArgumentException::new);

        String challenge = Challenge.getChallenge();
        enrollment.setSecret(challenge);
        enrollment.update(EnrollmentStatus.RETRIEVED);

        tiqrRepository.save(enrollment);
        //TODO Naming sucks big time now
        return new MetaData(this.service.addSecret(this.service, challenge), new Identity(enrollment));
    }

}
