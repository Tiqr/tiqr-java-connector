package tiqr.org.ocra;

import tiqr.org.secure.Challenge;
import tiqr.org.secure.OCRA;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OCRATest {

    private String hexString(String s) {
        return Hex.encodeHexString(s.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generateOCRA() {
        String sharedSecret = UUID.randomUUID().toString();
        String challenge = Challenge.generateQH10Challenge();
        String sessionKey = Challenge.generateSessionKey();

        String other = OCRA.generateOCRA(sharedSecret, challenge, sessionKey);

        String result = OCRA.generateOCRA(
                "OCRA-1:HOTP-SHA1-6:QH10-S064",
                hexString(sharedSecret),
                hexString("counter"),
                hexString(challenge),
                hexString("password"),
                hexString(sessionKey),
                hexString("timestamp"));

        assertEquals(result, other);
    }


}