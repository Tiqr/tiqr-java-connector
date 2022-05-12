package tiqr.org.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class APNSConfiguration {

    private String serverHost;
    private int port;
    private String signingKey;
    private String serverCertificateChain;
    private String topic;
    private String teamId;
    private String keyId;


}
