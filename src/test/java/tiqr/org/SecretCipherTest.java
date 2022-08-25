package tiqr.org;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecretCipherTest {

    private final SecretCipher secretCipher = new SecretCipher(UUID.randomUUID().toString());

    @Test
    void encrypt() {
        String sharedSecret = UUID.randomUUID().toString();
        String encrypted = secretCipher.encrypt(sharedSecret);
        String decrypted = secretCipher.decrypt(encrypted);
        assertEquals(sharedSecret, decrypted);
    }

    @Test
    void ensureThreadConsistency() {
        String secret = UUID.randomUUID().toString();
        SecretCipher firstSecretCipher = new SecretCipher(secret);
        SecretCipher secondSecretCipher = new SecretCipher(secret);

        String sharedSecret = UUID.randomUUID().toString();
        String encrypted = firstSecretCipher.encrypt(sharedSecret);
        String decrypted = secondSecretCipher.decrypt(encrypted);
        assertEquals(sharedSecret, decrypted);
    }

}