package eduid.secure;

import lombok.SneakyThrows;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SecretCipher {

    private final SecretKeySpec secretKey;

    @SneakyThrows
    public SecretCipher(String secret)  {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha.digest(secret.getBytes(UTF_8));
        this.secretKey = new SecretKeySpec(Arrays.copyOf(digest, 32), "AES");
    }

    @SneakyThrows
    public String encrypt(String sharedSecret)  {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(sharedSecret.getBytes(UTF_8)));    }

    @SneakyThrows
    public String decrypt(String encodedEncryptedSecret)  {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encodedEncryptedSecret)));    }
}
