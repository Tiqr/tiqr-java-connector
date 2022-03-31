package tiqr.org.secure;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.stream.Collectors;

public class Challenge {

    private static final SecureRandom secureRandom;

    static {
        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Challenge() {
    }

    public static String generateQH10Challenge() {
        return secureRandom.ints(10, 0, 16)
                .boxed()
                .map(Integer::toHexString)
                .collect(Collectors.joining());
    }

    public static String generateNonce() {
        byte[] aesKey = new byte[64];
        secureRandom.nextBytes(aesKey);
        String base64 = Base64.getEncoder().encodeToString(aesKey);
        return URLEncoder.encode(base64, Charset.defaultCharset()).replaceAll("%", "");
    }

    public static void verifyOcra(String secret, String challenge, String sessionKey, String expectedOcra) {
        String ocra = OCRA.generateOCRA(secret, challenge, sessionKey);
        boolean equals = MessageDigest.isEqual(ocra.getBytes(StandardCharsets.UTF_8), expectedOcra.getBytes(StandardCharsets.UTF_8));
        if (!equals) {
            throw new IllegalArgumentException(String.format("Response does not match. Expected %s, but got %s", expectedOcra, ocra));
        }
    }


}
