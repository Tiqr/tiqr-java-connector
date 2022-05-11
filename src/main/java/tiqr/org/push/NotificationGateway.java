package tiqr.org.push;

import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import tiqr.org.model.NotificationType;
import tiqr.org.model.Registration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class NotificationGateway implements PushNotifier {

    private final APNS apns;
    private final GCM gcm;

    @SneakyThrows
    public NotificationGateway(APNSConfiguration apnsConfiguration,
                               GCMConfiguration gcmConfiguration) {
        this.apns = new APNS(apnsConfiguration);
        this.gcm = new GCM(gcmConfiguration);
    }


    @Override
    public String push(Registration registration, String authorizationUrl) throws PushNotificationException {
        registration.validateForPushNotification();
        return registration.getNotificationType().equals(NotificationType.APNS.name()) ?
                apns.push(registration, authorizationUrl) : gcm.push(registration, authorizationUrl);
    }
}
