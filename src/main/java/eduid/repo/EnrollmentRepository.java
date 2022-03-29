package eduid.repo;

import eduid.model.Enrollment;
import eduid.model.Registration;

import java.util.Optional;

public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findEnrollmentByKey(String key);

    Optional<Enrollment> findEnrollmentByEnrollmentSecret(String key);
}
