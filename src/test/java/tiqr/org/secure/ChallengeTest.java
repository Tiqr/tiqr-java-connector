package tiqr.org.secure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChallengeTest {

    @Test
    void generateQH10Challenge() {
        String s = Challenge.generateQH10Challenge();
        assertEquals(10, s.length());
    }

    @Test
    void generateNonce() {
        assertEquals(128, Challenge.generateNonce().length());
    }

    @Test
    void generateSessionKey() {
        assertEquals(64, Challenge.generateSessionKey().length());
    }

    @Test
    void verifyOcra() {
        String sharedSecret = Challenge.generateNonce();
        String challenge = Challenge.generateQH10Challenge();
        String sessionKey = Challenge.generateSessionKey();

        String ocra = OCRA.generateOCRA(sharedSecret, challenge, sessionKey);
        Challenge.verifyOcra(sharedSecret, challenge, sessionKey, ocra);

        assertThrows(IllegalArgumentException.class, () -> Challenge.verifyOcra(sharedSecret, challenge, sessionKey, "nope"));
    }
}