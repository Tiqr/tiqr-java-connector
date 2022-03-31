package eduid.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationData {

    private String sessionKey;
    private String response;
    private String notificationAddress;

}
