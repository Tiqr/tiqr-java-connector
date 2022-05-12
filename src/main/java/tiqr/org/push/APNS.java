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
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import tiqr.org.model.Registration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public class APNS implements PushNotifier {

    private static final Log LOG = LogFactory.getLog(APNS.class);

    private final ApnsClient apnsClient;
    private final String topic;

    public APNS(APNSConfiguration apnsConfiguration) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource signingKeyResource = resourceLoader.getResource(apnsConfiguration.getSigningKey());
        ApnsClientBuilder apnsClientBuilder = new ApnsClientBuilder()
                .setApnsServer(apnsConfiguration.getServerHost(), apnsConfiguration.getPort())
                .setSigningKey(ApnsSigningKey.loadFromInputStream(signingKeyResource.getInputStream(),
                        apnsConfiguration.getTeamId(), apnsConfiguration.getKeyId()));
        //For integration testing
        if (StringUtils.hasText(apnsConfiguration.getServerCertificateChain())) {
            Resource serverCertificateChainResource = resourceLoader.getResource(apnsConfiguration.getServerCertificateChain());
            apnsClientBuilder
                    .setTrustedServerCertificateChain(serverCertificateChainResource.getInputStream());
        }
        this.apnsClient = apnsClientBuilder.build();
        this.topic = apnsConfiguration.getTopic();
    }

    public String push(Registration registration, String authorizationUrl) {
        String notificationAddress = registration.getNotificationAddress();
        String userId = registration.getUserId();

        ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
        payloadBuilder.setAlertBody("Please log in");
        payloadBuilder.addCustomProperty("challenge", authorizationUrl);

        String payload = payloadBuilder.build();
        SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(notificationAddress, this.topic, payload);

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
