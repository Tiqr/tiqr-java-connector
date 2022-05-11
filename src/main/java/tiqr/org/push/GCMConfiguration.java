package tiqr.org.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@AllArgsConstructor
@Getter
public class GCMConfiguration {

    private Resource firebaseServiceAccoun;
    private String appName;
}
