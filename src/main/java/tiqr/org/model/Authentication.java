package tiqr.org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Document(collection = "authentications")
public class Authentication {

    @Id
    private String id;

    private String userID;
    private String sessionKey;
    private String challenge;
    private AuthenticationStatus status;
    private Instant created;
    private Instant updated;

    public Authentication(String userID, String sessionKey, String challenge, AuthenticationStatus status) {
        this.userID = userID;
        this.sessionKey = sessionKey;
        this.challenge = challenge;
        this.status = status;
        this.created = Instant.now();
        this.updated = Instant.now();
    }

    public void update(AuthenticationStatus newStatus) {
        this.status = newStatus;
        this.updated = Instant.now();
    }
}
