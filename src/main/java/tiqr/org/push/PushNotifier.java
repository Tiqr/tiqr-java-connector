package tiqr.org.push;

import tiqr.org.model.Registration;

public interface PushNotifier {

    String push(Registration registration) throws PushNotificationException;

}
