package tiqr.org.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Setter
public class APNSConfiguration {

    private String serverHost;
    private int port;
    private Resource signingKey;
    private Optional<Resource> serverCertificateChain;
    private String teamId;
    private String keyId;


}
