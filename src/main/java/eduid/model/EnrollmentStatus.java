package eduid.model;

public enum EnrollmentStatus {

    INITIALIZED, // An enrollment session has begun
    RETRIEVED,   // The device has retrieved the metadata
    PROCESSED,   // The device has sent back a secret
    FINALIZED,   // The application has stored the secret
    VALIDATED;   // A first successful authentication was performed

}
