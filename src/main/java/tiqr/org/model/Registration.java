package tiqr.org.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Document(collection = "registrations")
@Setter
@ToString(exclude = {"id", "secret", "created", "updated", "enrollmentSecret"})
public class Registration implements Serializable {

    @Id
    private String id;

    private String userId;
    private String userDisplayName;
    private String secret;
    private String enrollmentSecret;
    private String language;
    private String notificationType;
    private String notificationAddress;
    private String operation;
    private RegistrationStatus status;
    private Instant created;
    private Instant updated;
    private boolean useRegistrationId;

    public void validateForInitialEnrollment() {
        validateForPushNotification();
        Assert.hasLength(enrollmentSecret, "enrollmentSecret is empty");
        Assert.notNull(status, "status is null");
    }

    public void validateForPushNotification() {
        Assert.hasLength(userId, "userId is empty");
        if (StringUtils.hasText(notificationType)) {
            NotificationType.valueOf(notificationType);
        }
    }
}
