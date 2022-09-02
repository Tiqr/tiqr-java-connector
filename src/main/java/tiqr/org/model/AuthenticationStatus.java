package tiqr.org.model;

public enum AuthenticationStatus {

    PENDING, // An authentication session has begun
    SUSPENDED, // The user is suspended because of rate limiting
    SUCCESS;  // The user is authenticated

}

