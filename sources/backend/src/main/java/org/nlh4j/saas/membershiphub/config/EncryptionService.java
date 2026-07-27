package org.nlh4j.saas.membershiphub.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * AES‑256 GCM encryption service for protecting PII fields.
 * <p>
 * The service reads a Base64‑encoded 256‑bit secret key from the
 * configuration property {@code encryption.key}.  It provides
 * deterministic, authenticated encryption using a random 12‑byte IV
 * per operation.  The resulting ciphertext is encoded as
 * {@code Base64(IV || CIPHERTEXT)}.
 * </p>
 *
 * <pre>
 * // Example usage
 * &#64;Inject EncryptionService encryptionService;
 *
 * String encrypted = encryptionService.encrypt("sensitive data");
 * String decrypted = encryptionService.decrypt(encrypted);
 * </pre>
 */
@ApplicationScoped
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12; // 96‑bit IV recommended for GCM
    private static final int KEY_LENGTH_BYTES = 32;    // 256‑bit key

    @Inject
    @ConfigProperty(name = "encryption.key")
    String base64Key;

    private SecretKeySpec secretKeySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    void init() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            if (keyBytes.length != KEY_LENGTH_BYTES) {
                throw new IllegalArgumentException(
                    "Invalid encryption key length: expected 256‑bit (32 bytes) but got " + keyBytes.length);
            }
            this.secretKeySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        } catch (IllegalArgumentException e) {
            throw new EncryptionException("Failed to initialise EncryptionService: invalid key configuration", e);
        }
    }

    /**
     * Encrypts the supplied plain‑text using AES‑256‑GCM.
     *
     * @param plainText the clear text to encrypt; may be {@code null}
     * @return Base64‑encoded string containing IV and ciphertext
     * @throws EncryptionException if encryption fails
     */
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concatenate IV + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a value previously produced by {@link #encrypt(String)}.
     *
     * @param cipherTextBase64 Base64‑encoded IV‖ciphertext string; may be {@code null}
     * @return the original plain text, or {@code null} if input was {@code null}
     * @throws EncryptionException if decryption fails (e.g., tampered data)
     */
    public String decrypt(String cipherTextBase64) {
        if (cipherTextBase64 == null) {
            return null;
        }
        try {
            byte[] cipherMessage = Base64.getDecoder().decode(cipherTextBase64);
            if (cipherMessage.length < GCM_IV_LENGTH_BYTES) {
                throw new EncryptionException("Ciphertext too short – missing IV");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);

            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }

    /**
     * Runtime exception wrapper for encryption‑related errors.
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message) {
            super(message);
        }

        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}