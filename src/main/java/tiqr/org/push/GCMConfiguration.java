package tiqr.org.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GCMConfiguration {

    private String firebaseServiceAccount;
    private String appName;
}
