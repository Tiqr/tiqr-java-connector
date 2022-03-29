package eduid.secure;

import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SecretCipherTest {

    private final SecretCipher secretCipher = new SecretCipher("1");

    @Test
    void encrypt() {
        String sharedSecret = UUID.randomUUID().toString();
        String encrypted = secretCipher.encrypt(sharedSecret);
        String decrypted = secretCipher.decrypt(encrypted);
        assertEquals(sharedSecret, decrypted);
    }

}