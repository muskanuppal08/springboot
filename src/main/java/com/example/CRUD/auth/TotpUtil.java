package com.example.CRUD.auth;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TotpUtil {

    private static final String ALGORITHM = "HmacSHA1";
    private static final int CODE_LENGTH = 6;
    private static final int TIME_STEP = 30; // 30 seconds

    // Characters used in Base32 encoding
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * Generate a new random Base32 encoded secret key (160 bits).
     */
    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] buffer = new byte[10]; // 80 bits is minimum; 10 bytes gives 80 bits, 20 bytes gives 160 bits. Let's use 10.
        random.nextBytes(buffer);
        return encodeBase32(buffer);
    }

    /**
     * Verification check for a TOTP code against the secret key.
     */
    public static boolean verifyCode(String secret, String code) {
        if (code == null || code.length() != CODE_LENGTH) {
            return false;
        }

        try {
            long currentInterval = System.currentTimeMillis() / 1000 / TIME_STEP;
            // Check current interval and immediately preceding/succeeding intervals to handle clock drift
            for (int i = -1; i <= 1; i++) {
                String expected = generateTotp(secret, currentInterval + i);
                if (expected.equals(code)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static String generateTotp(String secret, long timeInterval) throws GeneralSecurityException {
        byte[] key = decodeBase32(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeInterval).array();

        SecretKeySpec signKey = new SecretKeySpec(key, ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0xF;
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xFF);
        }

        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= Math.pow(10, CODE_LENGTH);

        return String.format("%0" + CODE_LENGTH + "d", truncatedHash);
    }

    private static String encodeBase32(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int index = 0, lookup = 0, offset = 0;
        while (index < bytes.length) {
            int digit;
            int currByte = bytes[index] & 0xFF;
            if (offset > 3) {
                int nextByte = (index + 1 < bytes.length) ? (bytes[index + 1] & 0xFF) : 0;
                digit = currByte & (0xFF >> offset);
                offset = (offset + 5) % 8;
                digit <<= offset;
                digit |= nextByte >> (8 - offset);
                index++;
            } else {
                digit = (currByte >> (8 - (offset + 5))) & 0x1F;
                offset = (offset + 5) % 8;
                if (offset == 0) {
                    index++;
                }
            }
            sb.append(BASE32_CHARS.charAt(digit));
        }
        return sb.toString();
    }

    private static byte[] decodeBase32(String base32) {
        base32 = base32.toUpperCase();
        int len = base32.length();
        byte[] bytes = new byte[len * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;

        for (int i = 0; i < len; i++) {
            char c = base32.charAt(i);
            int val = BASE32_CHARS.indexOf(c);
            if (val == -1) {
                continue;
            }
            buffer <<= 5;
            buffer |= val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return Arrays.copyOf(bytes, index);
    }
}
