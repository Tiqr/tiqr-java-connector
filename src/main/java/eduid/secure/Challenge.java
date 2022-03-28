package eduid.secure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public class Challenge {

    private Challenge() {
    }

    public static String getChallenge() {
        return UUID.randomUUID().toString();
    }

    public static boolean verifyChallenge(String challenge, String expected) {
        return MessageDigest.isEqual(challenge.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }


}
