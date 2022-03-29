package eduid.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Authentication {

    private String userID;
    private String userDisplayName;
    private String sessionKey;
    private String challenge;
    private AuthenticationStatus status;

    public void update(AuthenticationStatus newStatus) {
        this.status = newStatus;
    }
}
