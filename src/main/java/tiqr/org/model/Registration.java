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

    private String userId;
    private String secret;
    private String enrollmentSecret;
    private String language;
    private String notificationType;
    private String notificationAddress;
    private String operation;
    private Instant created;
    private Instant updated;

    public void validateForInitialEnrollment() {
        validateForPushNotification();
        Assert.hasLength(enrollmentSecret, "enrollmentSecret is empty");
    }

    public void validateForPushNotification() {
        Assert.hasLength(userId, "userId is empty");
        Assert.hasLength(notificationType, "notificationType is empty");
        Assert.hasLength(notificationAddress, "notificationAddress is empty");
        NotificationType.valueOf(notificationType);
    }
}
