package eduid.model;

public enum AuthenticationStatus {

    PENDING(1), // An authentication session has begun
    SUCCESS(2),   // The user is authenticated
    FAILURE(3);   // The user failed to authenticate

    private final int value;

    AuthenticationStatus(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }

}

