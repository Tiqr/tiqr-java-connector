package eduid.repo;

import eduid.model.Enrollment;
import eduid.model.Registration;

import java.util.Optional;

public interface RegistrationRepository {

    Registration save(Registration registration);

    Optional<Registration> findRegistrationByUserId(String userId);
}
