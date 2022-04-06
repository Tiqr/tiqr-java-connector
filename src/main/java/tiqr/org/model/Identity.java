package tiqr.org.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Identity {

    private String identifier;
    private String displayName;

    public Identity(Enrollment enrollment) {
        this.identifier = enrollment.getUserID();
        this.displayName = enrollment.getUserDisplayName();
    }
}
