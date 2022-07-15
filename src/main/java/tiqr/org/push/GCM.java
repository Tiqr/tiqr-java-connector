package tiqr.org.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import tiqr.org.model.Registration;

import java.io.IOException;
import java.util.Map;

public class GCM implements PushNotifier {

    private static final Log LOG = LogFactory.getLog(GCM.class);

    private final FirebaseMessaging firebaseMessaging;

    public GCM(GCMConfiguration gcmConfiguration) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();

        Resource firebaseServiceAccountResource = resourceLoader.getResource(gcmConfiguration.getFirebaseServiceAccount());
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(firebaseServiceAccountResource.getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions
                .builder()
                .setCredentials(googleCredentials)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, gcmConfiguration.getAppName());
        this.firebaseMessaging = FirebaseMessaging.getInstance(app);
    }

    public String push(Registration registration, String authorizationUrl) throws PushNotificationException {
        String notificationAddress = registration.getNotificationAddress();
        String userId = registration.getUserId();

        Message message = Message
                .builder()
                .setToken(notificationAddress)
                .putAllData(Map.of(
                        "text", "Please log in",
                        "challenge", authorizationUrl
                ))
                .build();
        try {
            String uuid = firebaseMessaging.send(message);

            LOG.debug(String.format("Push notification GCM send for user %s and token %s", userId, notificationAddress));

            return uuid;
        } catch (FirebaseMessagingException e) {
            throw new PushNotificationException(String.format(
                    "Error in push notification GCM for user %s and token %s", userId, notificationAddress
            ), e);
        }

    }
}
