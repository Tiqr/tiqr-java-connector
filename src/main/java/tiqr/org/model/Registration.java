package tiqr.org.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Document(collection = "registrations")
@Setter
public class Registration {

    @Id
    private String id;

    private String userId;
    private String secret;
    private String enrollmentSecret;
    private String language;
    private String notificationType;
    private String notificationAddress;
    private String version;
    private String operation;
    private Instant created;
    private Instant updated;

    public Registration(String userid, String notificationAddress) {
        this.userId = userid;
        this.notificationAddress = notificationAddress;
        this.created = Instant.now();
        this.updated = Instant.now();
    }

    public Registration(String userid,
                        String sharedSecret,
                        String enrollmentSecret,
                        String language,
                        String notificationType,
                        String notificationAddress,
                        String version,
                        String operation) {
        this.userId = userid;
        this.secret = sharedSecret;
        this.enrollmentSecret = enrollmentSecret;
        this.language = language;
        this.notificationType = notificationType;
        this.notificationAddress = notificationAddress;
        this.version = version;
        this.operation = operation;
        this.created = Instant.now();
        this.updated = Instant.now();
    }

    public void setSecret(String secret) {
        this.secret = secret;
        this.updated = Instant.now();
    }

    public void setNotificationAddress(String notificationAddress) {
        this.notificationAddress = notificationAddress;
        this.updated = Instant.now();
    }

    public void setEnrollmentSecret(String enrollmentSecret) {
        this.enrollmentSecret = enrollmentSecret;
        this.updated = Instant.now();
    }
}
