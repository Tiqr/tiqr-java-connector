package eduid.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Getter
public class Enrollment implements Serializable {

    private String key;
    private String enrollmentSecret;
    private String userID;
    private String userDisplayName;
    private EnrollmentStatus status;

    public Enrollment(String key, String userID, String userDisplayName, EnrollmentStatus status) {
        this.key = key;
        this.userID = userID;
        this.userDisplayName = userDisplayName;
        this.status = status;
    }

    public void update(EnrollmentStatus newStatus) {
        this.status = newStatus;
    }

    public void setEnrollmentSecret(String enrollmentSecret) {
        this.enrollmentSecret = enrollmentSecret;
    }

}
