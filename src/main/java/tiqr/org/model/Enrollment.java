package tiqr.org.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@Getter
@Document(collection = "enrollments")
public class Enrollment implements Serializable {

    @Id
    private String id;

    private String key;
    private String enrollmentSecret;
    private String userID;
    private String userDisplayName;
    private EnrollmentStatus status;
    private Instant created;
    private Instant updated;

    public Enrollment(String key, String userID, String userDisplayName, EnrollmentStatus status) {
        this.key = key;
        this.userID = userID;
        this.userDisplayName = userDisplayName;
        this.status = status;
        this.created = Instant.now();
        this.updated = Instant.now();
    }

    public void update(EnrollmentStatus newStatus) {
        this.status = newStatus;
        this.updated = Instant.now();
    }

    public void setEnrollmentSecret(String enrollmentSecret) {
        this.enrollmentSecret = enrollmentSecret;
        this.updated = Instant.now();
    }

}
