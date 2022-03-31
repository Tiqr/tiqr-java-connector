package tiqr.org.repo;

import tiqr.org.model.Authentication;

import java.util.Optional;

public interface AuthenticationRepository {

    Authentication save(Authentication authentication);

    Optional<Authentication> findAuthenticationBySessionKey(String sessionKey);

}
