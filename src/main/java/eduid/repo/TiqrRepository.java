package eduid.repo;

import eduid.model.Enrollment;

import java.util.Optional;

public interface TiqrRepository {

    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findEnrollmentByKey(String key);

}
