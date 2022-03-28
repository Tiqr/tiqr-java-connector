package eduid.model;

public enum EnrollmentStatus {

    IDLE(1), // Nothing happens
    INITIALIZED(2), // An enrollment session has begun
    RETRIEVED(3),   // The device has retrieved the metadata
    PROCESSED(4),   // The device has sent back a secret
    FINALIZED(5),// The application has stored the secret
    VALIDATED(6);   // A first succesful authentication was performed

    private final int status;

    EnrollmentStatus(int i) {
        this.status = i;
    }

    public int getStatus() {
        return status;
    }
}
