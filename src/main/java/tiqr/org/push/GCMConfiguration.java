package tiqr.org.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;

@AllArgsConstructor
@Getter
@Setter
public class GCMConfiguration {

    private Resource firebaseServiceAccount;
    private String appName;
}
