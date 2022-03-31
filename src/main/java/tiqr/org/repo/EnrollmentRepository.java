package tiqr.org.repo;

import tiqr.org.model.Enrollment;

import java.util.Optional;

public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findEnrollmentByKey(String key);

    Optional<Enrollment> findEnrollmentByEnrollmentSecret(String key);
}
