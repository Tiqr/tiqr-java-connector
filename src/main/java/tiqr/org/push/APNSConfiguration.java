package tiqr.org.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

import java.util.Optional;

@AllArgsConstructor
@Getter
public class APNSConfiguration {

    private String serverHost;
    private int port;
    private Resource signingKey;
    private Optional<Resource> serverCertificateChain;
    private String teamId;
    private String keyId;


}
