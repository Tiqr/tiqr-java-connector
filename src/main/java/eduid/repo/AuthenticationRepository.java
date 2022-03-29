package eduid.repo;

import eduid.model.Authentication;

import java.util.Optional;

public interface AuthenticationRepository {

    Authentication save(Authentication authentication);

    Optional<Authentication> findAuthenticationBySessionKey(String sessionKey);

}
