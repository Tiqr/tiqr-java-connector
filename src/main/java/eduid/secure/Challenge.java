package eduid.secure;

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

    public static String generateQN08Challenge() {
        return secureRandom.ints(8, 0, 10)
                .boxed()
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public static String generateNonce() {
        byte[] aesKey = new byte[64];
        secureRandom.nextBytes(aesKey);
        String base64 = Base64.getEncoder().encodeToString(aesKey);
        return URLEncoder.encode(base64, Charset.defaultCharset()).replaceAll("%", "");
    }

    public static boolean verifyOcra(String secret, String challenge, String expectedOcra) {
        String ocra = OCRA.generateOCRA(secret, challenge);
        return MessageDigest.isEqual(ocra.getBytes(StandardCharsets.UTF_8), expectedOcra.getBytes(StandardCharsets.UTF_8));
    }


}
