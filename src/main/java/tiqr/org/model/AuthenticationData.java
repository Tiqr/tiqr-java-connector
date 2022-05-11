package tiqr.org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationData {

    private String sessionKey;
    private String userId;
    private String response;
    private String language;
    private String operation;
    private String notificationType;
    private String notificationAddress;

    public AuthenticationData(String sessionKey, String response) {
        this.sessionKey = sessionKey;
        this.response = response;
    }
}
