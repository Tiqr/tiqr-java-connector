package tiqr.org;

import tiqr.org.model.*;

public interface TiqrService {
    /**
     * Start an enrollment. The enrollment will be persisted in order to check the status.
     *
     * @param userID the unique identifier of the User
     * @param userDisplayName displayName of the User
     * @return new Enrollment
     */
    Enrollment startEnrollment(String userID, String userDisplayName);

    /**
     * Retrieve the MetaData
     * @param enrollmentKey the unique key of the enrollment
     * @return the MetaData for the Tiqr app
     */
    MetaData getMetaData(String enrollmentKey) throws TiqrException;

    /**
     * Finish the enrollment
     * @param registration the form data from the Tiqr app
     * @return The updated Registration
     */
    Registration enrollData(Registration registration) throws TiqrException;

    /**
     * Finalize the registration after the User has provided a recovery method.
     * @param userId the unique identifier of the User
     * @return the finalized Registration
     */
    Registration finishRegistration(String userId) throws TiqrException;

    /**
     * Method to poll the status of the enrollment
     * @param enrollmentKey the unique key of the enrollment
     * @return the enrollment
     */
    Enrollment enrollmentStatus(String enrollmentKey) throws TiqrException;

    /**
     * Start an authentication
     * @param userId the unique identifier of the User
     * @param userDisplayName displayName of the User
     * @param eduIdAppBaseUrl the base URL of the eduID / Tiqr App
     * @param sendPushNotification indicator if we send push notifications
     * @return New Authentication
     */
    Authentication startAuthentication(String userId, String userDisplayName, String eduIdAppBaseUrl, boolean sendPushNotification) throws TiqrException;

    /**
     * Finish an authentication
     * @param authenticationData form data posted by the Tiqr app
     */
    void postAuthentication(AuthenticationData authenticationData) throws TiqrException;

    /**
     * Method to poll the status of the authentication
     * @param sessionKey the unique key of the authentication
     * @return the authentication
     */
    Authentication authenticationStatus(String sessionKey) throws TiqrException;

    /**
     *
     * @param registration valid Registration containing a secret
     * @return the decrypted secret
     */
    String decryptRegistrationSecret(Registration registration);
}
