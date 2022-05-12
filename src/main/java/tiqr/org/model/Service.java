package tiqr.org.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.util.UriComponentsBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Service {

    private final String ocraSuite = "OCRA-1:HOTP-SHA1-6:QH10-S064";

    private String displayName;
    private String identifier;
    private String version;
    private String logoUrl;
    private String infoUrl;
    private String authenticationUrl;
    private boolean pushNotificationsEnabled;
    private String enrollmentUrl;

    public static Service addEnrollmentSecret(Service baseService, String enrollmentSecret) {
        return new Service(
                baseService.getDisplayName(),
                baseService.getIdentifier(),
                baseService.getVersion(),
                baseService.getLogoUrl(),
                baseService.getInfoUrl(),
                baseService.getAuthenticationUrl(),
                baseService.isPushNotificationsEnabled(),
                UriComponentsBuilder
                        .fromHttpUrl(baseService.enrollmentUrl)
                        .queryParam("enrollment_secret", enrollmentSecret)
                        .toUriString()
        );
    }
}
