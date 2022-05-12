package tiqr.org.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GCMConfiguration {

    private Resource firebaseServiceAccount;
    private String appName;
}
