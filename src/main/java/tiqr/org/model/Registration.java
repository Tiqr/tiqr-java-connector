package tiqr.org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Registration {

    private String userid;
    private String secret;
    private String enrollmentSecret;
    private String language;
    private String notificationType;
    private String notificationAddress;
    private String version;
    private String operation;

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setNotificationAddress(String notificationAddress) {
        this.notificationAddress = notificationAddress;
    }
}
