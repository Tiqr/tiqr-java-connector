package eduid.model;

public enum EnrollmentStatus {

    INITIALIZED(1), // An enrollment session has begun
    RETRIEVED(2),   // The device has retrieved the metadata
    PROCESSED(3),   // The device has sent back a secret
    FINALIZED(4),   // The application has stored the secret
    VALIDATED(5);   // A first successful authentication was performed

    private final int status;

    EnrollmentStatus(int i) {
        this.status = i;
    }

    public int getStatus() {
        return status;
    }
}
