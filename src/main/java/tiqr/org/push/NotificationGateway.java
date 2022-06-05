package tiqr.org.push;

import lombok.SneakyThrows;
import tiqr.org.model.NotificationType;
import tiqr.org.model.Registration;

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
        switch (NotificationType.valueOf(registration.getNotificationType())) {
            case FCM:
            case GCM:
            case FCM_DIRECT:
                return gcm.push(registration, authorizationUrl);
            case APNS:
            case APNS_DIRECT:
                return apns.push(registration, authorizationUrl);
            default:
                throw new IllegalArgumentException("Unknown NotificationType" + registration.getNotificationType());
        }
    }
}
