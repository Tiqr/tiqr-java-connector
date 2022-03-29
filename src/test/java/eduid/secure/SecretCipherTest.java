package eduid.secure;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecretCipherTest {

    private final SecretCipher secretCipher = new SecretCipher("1");

    @Test
    void encrypt() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String sharedSecret = UUID.randomUUID().toString();
        String encrypted = secretCipher.encrypt(sharedSecret);
        String decrypted = secretCipher.decrypt(encrypted);
        assertEquals(sharedSecret, decrypted);
    }

}