package eduid.ocra;

import eduid.secure.OCRA;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OCRATest {

    private String hexString(String s) {
        return Hex.encodeHexString(s.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generateOCRA() {
        String result = OCRA.generateOCRA(
                "OCRA-1:HOTP-SHA1-6:QN08",
                hexString("secret"),
                hexString("counter"),
                hexString("question"),
                hexString("password"),
                hexString("sessionInformation"),
                hexString("timestamp"));
        assertEquals("380884", result);

        String other = OCRA.generateOCRA("secret", "question");
        assertEquals(result, other);
    }


}