package tiqr.org.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;

import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class APNSConfiguration {

    private String serverHost;
    private int port;
    private Resource signingKey;
    private Optional<Resource> serverCertificateChain;
    private String topic;
    private String teamId;
    private String keyId;


}
