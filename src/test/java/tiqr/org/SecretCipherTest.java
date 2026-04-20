package tiqr.org;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
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

    @Test
    void migration() {
        String secret = UUID.randomUUID().toString();
        SecretCipher secretCipher = new SecretCipher(secret);

        String encryptedLegacy = encryptLegacy(secretCipher,secret);
        String decryptedLegacy = secretCipher.decryptLegacy(encryptedLegacy);
        assertEquals(secret, decryptedLegacy);

        String encrypted  = secretCipher.encrypt(decryptedLegacy);
        String decrypted = secretCipher.decrypt(encrypted);
        assertEquals(secret, decrypted);
    }

    @SneakyThrows
    public static String encryptLegacy(SecretCipher secretCipher, String sharedSecret)  {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        Key secretKey = (Key) ReflectionTestUtils.getField(secretCipher, "secretKey");
        GCMParameterSpec parameterSpec = (GCMParameterSpec) ReflectionTestUtils.getField(secretCipher, "parameterSpec");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        return Base64.getEncoder().encodeToString(cipher.doFinal(sharedSecret.getBytes(UTF_8)));
    }
}