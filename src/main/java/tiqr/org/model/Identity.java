package tiqr.org.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Identity {

    private String identifier;
    private String displayName;

    public Identity(Enrollment enrollment) {
        this.identifier = enrollment.getRegistrationId();
        this.displayName = enrollment.getUserDisplayName();
    }
}
