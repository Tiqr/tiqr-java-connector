package tiqr.org.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

import java.time.Instant;

@Getter
@Document(collection = "registrations")
@Setter
public class Registration {

    @Id
    private String id;

    private String userid;
    private String secret;
    private String enrollmentSecret;
    private String language;
    private String notificationType;
    private String notificationAddress;
    private String version;
    private String operation;
    private Instant created;
    private Instant updated;

    public Registration() {
        this(null, null);
    }

    public Registration(String userid, String notificationAddress) {
        this(userid, null, null, null, null, notificationAddress, null, null);
    }

    public Registration(String userid,
                        String sharedSecret,
                        String enrollmentSecret,
                        String language,
                        String notificationType,
                        String notificationAddress,
                        String version,
                        String operation) {
        this.userid = userid;
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

    public void validateForInitialEnrollment() {
        Assert.hasLength(userid, "userid is empty");
        Assert.hasLength(enrollmentSecret, "enrollmentSecret is empty");
        Assert.hasLength(notificationAddress, "notificationAddress is empty");
    }
}
