package tiqr.org.ocra;

import tiqr.org.secure.OCRA;
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
                "OCRA-1:HOTP-SHA1-6:QH10",
                hexString("secret"),
                hexString("counter"),
                hexString("question"),
                hexString("password"),
                hexString("sessionInformation"),
                hexString("timestamp"));
        assertEquals("326413", result);

        String other = OCRA.generateOCRA("secret", "question", "sessionInformation");
        assertEquals(result, other);
    }


}