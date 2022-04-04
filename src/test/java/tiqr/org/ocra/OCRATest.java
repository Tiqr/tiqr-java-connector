package tiqr.org.ocra;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import tiqr.org.secure.Challenge;
import tiqr.org.secure.OCRA;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OCRATest {

    private String hexString(String s) {
        return Hex.encodeHexString(s.getBytes(StandardCharsets.UTF_8));
    }

    public String sessionKey = "3132333435363738393031323334353637383930313233343536373839303132";

    private List<Input> inputList = List.of(
            new Input("0000000000", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", "174452")
    );

    @Test
    void validateInput() {
        inputList.forEach(input -> {
            assertEquals(input.expectedOcra, OCRA.generateOCRA(input.sharedSecret, input.challenge, sessionKey));
        });
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

    private class Input {
        public String challenge;
        public String sharedSecret;
        public String expectedOcra;

        public Input(String challenge, String sharedSecret, String expectedOcra) {
            this.challenge = challenge;
            this.sharedSecret = sharedSecret;
            this.expectedOcra = expectedOcra;
        }
    }


}