package tiqr.org.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "authentications")
public class Authentication implements Serializable {

    @Id
    private String id;

    private String userID;
    private String userDisplayName;
    private String sessionKey;
    private String challenge;
    private AuthenticationStatus status;
    private String authenticationUrl;
    private Instant created;
    private Instant updated;

    public Authentication(String userID, String userDisplayName, String sessionKey, String challenge, String authenticationUrl, AuthenticationStatus status) {
        this.userID = userID;
        this.userDisplayName = userDisplayName;
        this.sessionKey = sessionKey;
        this.challenge = challenge;
        this.authenticationUrl = authenticationUrl;
        this.status = status;
        this.created = Instant.now();
        this.updated = Instant.now();
    }

    public void update(AuthenticationStatus newStatus) {
        this.status = newStatus;
        this.updated = Instant.now();
    }
}
