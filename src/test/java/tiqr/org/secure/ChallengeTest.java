package tiqr.org.secure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChallengeTest {

    @Test
    void generateQH08Challenge() {
        assertEquals(8, Challenge.generateQN08Challenge().length());
    }

    @Test
    void generateNonce() {
        assertTrue(90 <= Challenge.generateNonce().length());
        assertTrue(102 >= Challenge.generateNonce().length());
    }

    @Test
    void verifyOcra() {
        String ocra = OCRA.generateOCRA("sharedSecret", "12345678", "sessionKey");
        Challenge.verifyOcra("sharedSecret", "12345678", "sessionKey", ocra);

        assertThrows(IllegalArgumentException.class, () -> Challenge.verifyOcra("sharedSecret", "12345678", "sessionKey", "nope"));
    }
}