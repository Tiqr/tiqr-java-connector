package tiqr.org.push;

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

    public NotificationGateway(String serverHost,
                               int port,
                               Resource signingKey,
                               Optional<Resource> serverCertificateChain,
                               String teamId,
                               String keyId,
                               Resource firebaseServiceAccount,
                               String appName) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        this.apns = new APNS(serverHost, port, signingKey, serverCertificateChain, teamId, keyId);
        this.gcm = new GCM(firebaseServiceAccount, appName);
    }


    @Override
    public String push(Registration registration) throws PushNotificationException {
        registration.validateForPushNotification();
        return registration.getNotificationType().equals(NotificationType.APNS.name()) ? apns.push(registration) : gcm.push(registration);
    }
}
