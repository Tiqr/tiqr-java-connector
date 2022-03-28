package eduid.ocra;

import eduid.crypto.KeyGenerator;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

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

        String other = OCRA.generateOCRA(
                hexString("secret"),
                hexString("question"));
        assertEquals(result, other);
    }

    @Test
    void encrypt() throws Exception {
        String[] keys = KeyGenerator.generateKeys();
        String pem = keys[0];
        String certificate = keys[1];

        PrivateKey privateKey = KeyGenerator.readPrivateKey(pem);
        byte[] certBytes = KeyGenerator.getDER(certificate);
        X509Certificate x509Certificate = KeyGenerator.getCertificate(certBytes);


        final Cipher rsa = Cipher.getInstance("RSA");
        String message = UUID.randomUUID().toString();
        rsa.init(Cipher.ENCRYPT_MODE, x509Certificate);
        rsa.update(message.getBytes());
        final byte[] result = rsa.doFinal();

        System.out.println("Message: " + message);
        System.out.println("Encrypted: " + DatatypeConverter.printHexBinary(result));

        rsa.init(Cipher.DECRYPT_MODE, privateKey);
        rsa.update(result);
        String decrypted = new String(rsa.doFinal());

        System.out.println("Decrypted: " + decrypted);
    }

    @SneakyThrows
    private String read(Resource resource) {
        return IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
    }


}