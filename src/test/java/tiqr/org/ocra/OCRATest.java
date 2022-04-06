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

    public String sharedSecret = "3132333435363738393031323334353637383930313233343536373839303132";

    private List<Input> inputList = List.of(
            new Input("0000000000", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", "174452"),
            new Input("1111111111", "202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f", "554036"),
            new Input("2222222222", "404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f", "468209"),
            new Input("3333333333", "606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f", "445556"),
            new Input("4444444444", "808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9f", "407436"),
            new Input("5555555555", "a0a1a2a3a4a5a6a7a8a9aaabacadaeafb0b1b2b3b4b5b6b7b8b9babbbcbdbebf", "645826"),
            new Input("6666666666", "c0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedf", "485668"),
            new Input("7777777777", "e0e1e2e3e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff", "246775"),
            new Input("8888888888", "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", "242998"),
            new Input("9999999999", "202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f", "597774"),
            new Input("aaaaaaaaaa", "404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f", "165023"),
            new Input("bbbbbbbbbb", "606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f", "940705"),
            new Input("cccccccccc", "808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9f", "780450"),
            new Input("dddddddddd", "a0a1a2a3a4a5a6a7a8a9aaabacadaeafb0b1b2b3b4b5b6b7b8b9babbbcbdbebf", "261967"),
            new Input("eeeeeeeeee", "c0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedf", "638400"),
            new Input("ffffffffff", "e0e1e2e3e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff", "464175")
    );


    @Test
    void validateInput() {
        inputList.forEach(input -> assertEquals(input.expectedOcra, verifyOcra(sharedSecret, input.challenge, input.sessionInformation)));
    }

    @Test
    void generateOCRAWithDefaults() {
        String sharedSecret = Challenge.generateNonce();
        String challenge = Challenge.generateQH10Challenge();
        String sessionInformation = Challenge.generateSessionKey();

        verifyOcra(sharedSecret, challenge, sessionInformation);
    }

    @Test
    void generateOCRAWithUUID() {
        String sharedSecret = Challenge.generateNonce();
        String challenge = Challenge.generateQH10Challenge();
        String sessionInformation = UUID.randomUUID().toString();
        String sessionInformationHex = Hex.encodeHexString(sessionInformation.getBytes(StandardCharsets.UTF_8));

        verifyOcra(sharedSecret, challenge, sessionInformationHex);
    }

    private String verifyOcra(String sharedSecret, String challenge, String sessionInformation) {
        String other = OCRA.generateOCRA(sharedSecret, challenge, sessionInformation);
        String result = OCRA.generateOCRA(
                "OCRA-1:HOTP-SHA1-6:QH10-S064",
                sharedSecret,
                null,
                challenge,
                null,
                sessionInformation,
                null);

        assertEquals(result, other);
        return result;
    }

    private class Input {
        public String challenge;
        public String sessionInformation;
        public String expectedOcra;

        public Input(String challenge, String sessionInformation, String expectedOcra) {
            this.challenge = challenge;
            this.sessionInformation = sessionInformation;
            this.expectedOcra = expectedOcra;
        }
    }


}