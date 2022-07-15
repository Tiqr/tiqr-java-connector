package tiqr.org.push;

public class PushNotificationException extends Exception {

    public PushNotificationException(String message) {
        super(message);
    }

    public PushNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
