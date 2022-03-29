package eduid.secure;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;


/**
 * This is an implementation of the OCRA spec. It is based on the reference
 * implementation in http://tools.ietf.org/html/rfc6287
 *
 * Copy of https://github.com/SURFnet/ocra-implementations/blob/master/java/src/nl/surfnet/ocra/OCRA.java
 */
public class OCRA {

    private OCRA() {
    }

    /**
     * This method uses the JCE to provide the crypto
     * algorithm.
     * HMAC computes a Hashed Message Authentication Code with the
     * crypto hash algorithm as a parameter.
     *
     * @param crypto   the crypto algorithm (HmacSHA1,
     *                 HmacSHA256,
     *                 HmacSHA512)
     * @param keyBytes the bytes to use for the HMAC key
     * @param text     the message or text to be authenticated.
     */
    private static byte[] hmac_sha1(String crypto, byte[] keyBytes, byte[] text) {
        Mac hmac;

        try {
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey =
                    new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

    }

    private static String addLeadingZeros(String s, int i) {
        StringBuilder builder = new StringBuilder(s);
        while (builder.length() < i) {
            builder.insert(0, "0");
        }
        return builder.toString();
    }

    private static final int[] DIGITS_POWER
            // 0 1  2   3    4     5      6       7        8
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    /**
     * This method converts HEX string to Byte[]
     *
     * @param hex the HEX string
     * @return A byte array
     */

    private static byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    /**
     * This method generates an OCRA HOTP value for the QN08 variant
     *
     * @param sharedSecret       the shared secret between the two parties
     * @param challenge          the 8-numeric challenge question
     * @return A numeric String in base 10 that includes truncationDigits digits
     */
    static public String generateOCRA(String sharedSecret,
                                      String challenge) {
        return generateOCRA("OCRA-1:HOTP-SHA1-6:QN08",
                Hex.encodeHexString(sharedSecret.getBytes(StandardCharsets.UTF_8)),
                null,
                Hex.encodeHexString(challenge.getBytes(StandardCharsets.UTF_8)),
                null,
                null,
                null);
    }
    /**
     * This method generates an OCRA HOTP value for the given
     * set of parameters.
     *
     * @param ocraSuite          the OCRA Suite
     * @param key                the shared secret, HEX encoded
     * @param counter            the counter that changes
     *                           on a per-use basis,
     *                           HEX encoded
     * @param question           the challenge question, HEX encoded
     * @param password           a password that can be used,
     *                           HEX encoded
     * @param sessionInformation Static information that identifies the
     *                           current session, Hex encoded
     * @param timeStamp          a value that reflects a time
     * @return A numeric String in base 10 that includes truncationDigits digits
     */
    static public String generateOCRA(String ocraSuite,
                                      String key,
                                      String counter,
                                      String question,
                                      String password,
                                      String sessionInformation,
                                      String timeStamp) {
        int codeDigits;
        String crypto = "";
        int ocraSuiteLength = (ocraSuite.getBytes()).length;
        int counterLength = 0;
        int questionLength = 0;
        int passwordLength = 0;

        int sessionInformationLength = 0;
        int timeStampLength = 0;

        // The OCRASuites components
        String[] splitOcraSuite = ocraSuite.split(":");
        String cryptoFunction = splitOcraSuite[1];
        String dataInput = splitOcraSuite[2];

        if (cryptoFunction.toLowerCase().indexOf("sha1") > 1)
            crypto = "HmacSHA1";
        if (cryptoFunction.toLowerCase().indexOf("sha256") > 1)
            crypto = "HmacSHA256";
        if (cryptoFunction.toLowerCase().indexOf("sha512") > 1)
            crypto = "HmacSHA512";

        // How many digits should we return
        codeDigits = Integer.decode(cryptoFunction.substring
                (cryptoFunction.lastIndexOf("-") + 1));

        // The size of the byte array message to be encrypted
        // Counter
        String dataInputLower = dataInput.toLowerCase();

        if (dataInputLower.startsWith("c")) {
            // Fix the length of the HEX string
            counter = addLeadingZeros(counter, 16);
            counterLength = 8;
        }
        // Question - always 128 bytes
        if (dataInputLower.startsWith("q") ||
                (dataInputLower.contains("-q"))) {
            StringBuilder questionBuilder = new StringBuilder(question);
            while (questionBuilder.length() < 256) {
                questionBuilder.append("0");
            }
            question = questionBuilder.toString();
            questionLength = 128;
        }

        // Password - sha1
        if (dataInputLower.indexOf("psha1") > 1) {
            password = addLeadingZeros(password, 40);

            passwordLength = 20;
        }

        // Password - sha256
        if (dataInputLower.indexOf("psha256") > 1) {
            password = addLeadingZeros(password, 64);

            passwordLength = 32;
        }

        // Password - sha512
        if (dataInputLower.indexOf("psha512") > 1) {
            password = addLeadingZeros(password, 128);

            passwordLength = 64;
        }

        // sessionInformation - s064
        if (dataInputLower.indexOf("s064") > 1) {
            sessionInformation = addLeadingZeros(sessionInformation, 128);
            sessionInformationLength = 64;
        }

        // sessionInformation - s128
        if (dataInputLower.indexOf("s128") > 1) {
            sessionInformation = addLeadingZeros(sessionInformation, 256);

            sessionInformationLength = 128;
        }

        // sessionInformation - s256
        if (dataInputLower.indexOf("s256") > 1) {
            sessionInformation = addLeadingZeros(sessionInformation, 512);

            sessionInformationLength = 256;
        }

        // sessionInformation - s512
        if (dataInputLower.indexOf("s512") > 1) {
            sessionInformation = addLeadingZeros(sessionInformation, 1024);
            sessionInformationLength = 512;
        }

        // TimeStamp
        if (dataInputLower.startsWith("t") ||
                (dataInputLower.indexOf("-t") > 1)) {
            timeStamp = addLeadingZeros(timeStamp, 16);

            timeStampLength = 8;
        }

        // Remember to add "1" for the "00" byte delimiter
        byte[] msg = new byte[ocraSuiteLength +
                counterLength +
                questionLength +
                passwordLength +
                sessionInformationLength +
                timeStampLength +
                1];


        // Put the bytes of "ocraSuite" parameters into the message
        byte[] bArray = ocraSuite.getBytes();
        System.arraycopy(bArray, 0, msg, 0, bArray.length);

        // Delimiter
        msg[bArray.length] = 0x00;

        // Put the bytes of "Counter" to the message
        // Input is HEX encoded
        if (counterLength > 0) {
            bArray = hexStr2Bytes(counter);
            System.arraycopy(bArray, 0, msg, ocraSuiteLength + 1,
                    bArray.length);
        }


        // Put the bytes of "question" to the message
        // Input is text encoded
        if (questionLength > 0) {
            bArray = hexStr2Bytes(question);
            System.arraycopy(bArray, 0, msg, ocraSuiteLength + 1 +
                    counterLength, bArray.length);
        }

        // Put the bytes of "password" to the message
        // Input is HEX encoded
        if (passwordLength > 0) {
            bArray = hexStr2Bytes(password);
            System.arraycopy(bArray, 0, msg, ocraSuiteLength + 1 +
                    counterLength + questionLength, bArray.length);

        }

        // Put the bytes of "sessionInformation" to the message
        // Input is text encoded
        if (sessionInformationLength > 0) {
            bArray = hexStr2Bytes(sessionInformation);
            System.arraycopy(bArray, 0, msg, ocraSuiteLength + 1 +
                    counterLength + questionLength +
                    passwordLength, bArray.length);
        }

        // Put the bytes of "time" to the message
        // Input is text value of minutes
        if (timeStampLength > 0) {
            bArray = hexStr2Bytes(timeStamp);
            System.arraycopy(bArray, 0, msg, ocraSuiteLength + 1 +
                            counterLength + questionLength +
                            passwordLength + sessionInformationLength,
                    bArray.length);
        }

        bArray = hexStr2Bytes(key);

        byte[] hash = hmac_sha1(crypto, bArray, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary =
                ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];
        return addLeadingZeros(Integer.toString(otp), codeDigits);
    }

}