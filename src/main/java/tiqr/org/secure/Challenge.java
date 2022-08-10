package tiqr.org.secure;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.Collectors;

public class Challenge {

    private static final SecureRandom secureRandom = new SecureRandom();

    private Challenge() {
    }

    public static String generateQH10Challenge() {
        return secureRandom.ints(10, 0, 16)
                .boxed()
                .map(Integer::toHexString)
                .collect(Collectors.joining());
    }

    public static String generateNonce() {
        return generateRandom(64);
    }

    public static String generateSessionKey() {
        return generateRandom(32);
    }

    private static String generateRandom(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Hex.encodeHexString(bytes);
    }

    public static void verifyOcra(String secret, String challenge, String sessionKey, String expectedOcra) {
        String ocra = OCRA.generateOCRA(secret, challenge, sessionKey);
        boolean equals = MessageDigest.isEqual(ocra.getBytes(StandardCharsets.UTF_8), expectedOcra.getBytes(StandardCharsets.UTF_8));
        if (!equals) {
            throw new IllegalArgumentException(String.format("Response does not match. Expected %s, but got %s", ocra, expectedOcra));
        }
    }


}
