package tiqr.org.push;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import tiqr.org.model.Registration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class APNS implements PushNotifier {

    private static final Log LOG = LogFactory.getLog(APNS.class);

    final ApnsClient apnsClient;

    public APNS(String serverHost,
                int port,
                Resource signingKey,
                Optional<Resource> serverCertificateChain,
                String teamId,
                String keyId) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        ApnsClientBuilder apnsClientBuilder = new ApnsClientBuilder()
                .setApnsServer(serverHost, port)
                .setSigningKey(ApnsSigningKey.loadFromInputStream(signingKey.getInputStream(),
                        teamId, keyId));
        //For integration testing
        if (serverCertificateChain.isPresent()) {
            apnsClientBuilder
                    .setTrustedServerCertificateChain(serverCertificateChain.get().getInputStream());
        }
        this.apnsClient = apnsClientBuilder.build();
    }

    public String push(Registration registration) {
        String notificationAddress = registration.getNotificationAddress();
        String userId = registration.getUserId();

        ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
        payloadBuilder.setCategoryName("tiqr");

        String payload = payloadBuilder.build();
        SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(notificationAddress, "tiqr", payload);

        try {
            PushNotificationResponse<SimpleApnsPushNotification> response = this.apnsClient.sendNotification(pushNotification).get();
            String id = response.getApnsId().toString();

            LOG.info(String.format("Push notification APNS send for user %s and token %s", userId, notificationAddress));

            return id;
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            throw new PushNotificationException(String.format(
                    "Error in push notification APNS for user %s and token %s", userId, notificationAddress
            ), e);
        }
    }
}
