package tiqr.org;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

class SecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private final SecretKeySpec secretKey;
    private final GCMParameterSpec parameterSpec;

    @SneakyThrows
    SecretCipher(String secret) {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha.digest(secret.getBytes(UTF_8));
        this.secretKey = new SecretKeySpec(Arrays.copyOf(digest, 32), "AES");
        this.parameterSpec = new GCMParameterSpec(128, secret.getBytes(UTF_8));
    }

    @SneakyThrows
    public String encrypt(String sharedSecret) {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        return Base64.getEncoder().encodeToString(cipher.doFinal(sharedSecret.getBytes(UTF_8)));
    }

    @SneakyThrows
    public String decrypt(String encodedEncryptedSecret) {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encodedEncryptedSecret)));
    }
}
