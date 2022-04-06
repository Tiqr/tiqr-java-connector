package tiqr.org.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import tiqr.org.model.Registration;

import java.io.IOException;

public class GCM implements PushNotifier {

    private static final Log LOG = LogFactory.getLog(GCM.class);

    private final FirebaseMessaging firebaseMessaging;

    public GCM(Resource firebaseServiceAccount, String appName) throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(firebaseServiceAccount.getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions
                .builder()
                .setCredentials(googleCredentials)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, appName);
        this.firebaseMessaging = FirebaseMessaging.getInstance(app);
    }

    public String push(Registration registration) {
        String notificationAddress = registration.getNotificationAddress();
        String userId = registration.getUserid();

        Notification notification = Notification.builder().setBody("tiqr-java-connector").build();
        Message message = Message
                .builder()
                .setToken(notificationAddress)
                .setNotification(notification)
                .build();
        try {
            String uuid = firebaseMessaging.send(message);

            LOG.info(String.format("Push notification GCM send for user % and token %s", userId, notificationAddress));

            return uuid;
        } catch (FirebaseMessagingException e) {
            throw new PushNotificationException(String.format(
                    "Error in push notification GCM for user % and token %s", userId, notificationAddress
            ), e);
        }

    }
}
