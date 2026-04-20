package tiqr.org;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;   // 96-bit IV — NIST recommended
    private static final int GCM_TAG_LENGTH = 128;  // 128-bit auth tag
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SecretKeySpec secretKey;
    private final GCMParameterSpec parameterSpec;


    @SneakyThrows
    public SecretCipher(String secret) {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha.digest(secret.getBytes(UTF_8));
        this.secretKey = new SecretKeySpec(Arrays.copyOf(digest, 32), "AES");
        this.parameterSpec = new GCMParameterSpec(128, secret.getBytes(UTF_8));
    }

    @SneakyThrows
    public String encrypt(String sharedSecret) {
        byte[] plaintext = sharedSecret.getBytes(UTF_8);
        byte[] iv = new byte[GCM_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] ciphertext = cipher.doFinal(plaintext);

        // Prepend IV so the GCMParameterSpec can be reconstructed
        byte[] output = new byte[GCM_IV_LENGTH + ciphertext.length];
        System.arraycopy(iv, 0, output, 0, GCM_IV_LENGTH);
        System.arraycopy(ciphertext, 0, output, GCM_IV_LENGTH, ciphertext.length);
        return Base64.getEncoder().encodeToString(output);
    }

    @SneakyThrows
    public String decryptLegacy(String encodedEncryptedSecret) {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encodedEncryptedSecret)));
    }

    @SneakyThrows
    public String decrypt(String encodedEncryptedSecret) {
        byte[] input = Base64.getDecoder().decode(encodedEncryptedSecret);

        if (input.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Input too short to contain IV");
        }

        byte[] iv = Arrays.copyOfRange(input, 0, GCM_IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(input, GCM_IV_LENGTH, input.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return new String(cipher.doFinal(ciphertext), UTF_8);
    }
}
