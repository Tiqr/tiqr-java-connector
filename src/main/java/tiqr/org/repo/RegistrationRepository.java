package tiqr.org.repo;

import tiqr.org.model.Registration;

import java.util.Optional;

public interface RegistrationRepository {

    Registration save(Registration registration);

    Optional<Registration> findRegistrationByUserId(String userId);

    Long deleteByUserId(String userId);

    void delete(Registration entity);
}
