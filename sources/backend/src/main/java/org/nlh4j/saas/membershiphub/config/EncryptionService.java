package org.nlh4j.saas.membershiphub.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * AES‑256 encryption service used for protecting PII fields.
 * <p>
 * The secret key is supplied via the {@code encryption.key} configuration property
 * and must be a Base64‑encoded 256‑bit key (32 bytes). Encryption is performed
 * with {@code AES/GCM/NoPadding} to provide confidentiality and integrity.
 * The generated IV (12 bytes) is prefixed to the ciphertext and the whole payload
 * is Base64‑encoded for storage.
 * </p>
 */
@ApplicationScoped
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int IV_LENGTH = 12;       // 96‑bit recommended for GCM

    private final SecretKeySpec secretKey;

    /**
     * Constructs the service injecting the Base64‑encoded AES‑256 key.
     *
     * @param base64Key the Base64 representation of a 32‑byte key
     */
    public EncryptionService(@ConfigProperty(name = "encryption.key") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Invalid AES‑256 key length; expected 32 bytes");
        }
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Encrypts the supplied plain text.
     *
     * @param plainText the clear text to encrypt
     * @return Base64‑encoded string containing IV + ciphertext
     */
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // Generate a fresh IV for each encryption
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Prefix IV to ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buffer.put(iv);
            buffer.put(cipherBytes);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a previously encrypted value.
     *
     * @param encryptedBase64 Base64 string containing IV + ciphertext
     * @return the original clear text
     */
    public String decrypt(String encryptedBase64) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] cipherBytes = new byte[buffer.remaining()];
            buffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }

    /**
     * Runtime exception wrapper for encryption/decryption errors.
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}