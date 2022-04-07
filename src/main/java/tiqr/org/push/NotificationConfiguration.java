package tiqr.org.push;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class NotificationConfiguration {

    private String apnsServerHost;
    private int apnsPort;
    private Resource apnsSigningKeyResource;
    private Optional<Resource> apnsServerCertificateChainResource;
    private String apnsTeamId;
    private String apnsKeyId;
    private Resource googleFirebaseServiceAccountResource;
    private String googleAppName;

}
