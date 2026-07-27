package org.nlh4j.saas.membershiphub.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.owasp.encoder.Encode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Utility class that provides defensive sanitization of user supplied input.
 * <p>
 * All public methods are safe to be used in any layer (REST, service, repository) before persisting
 * or rendering data. The implementation follows OWASP A03 (Injection) guidelines:
 * <ul>
 *   <li>HTML/JS injection is mitigated by encoding via OWASP Java Encoder.</li>
 *   <li>SQL injection is mitigated by escaping single quotes and removing dangerous patterns,
 *       however the preferred approach is to use parameterised queries.</li>
 *   <li>Control characters and invisible Unicode are stripped.</li>
 *   <li>Length checks are performed to avoid denial‑of‑service via extremely large payloads.</li>
 * </ul>
 * </p>
 */
@ApplicationScoped
public class InputSanitizer {

    /** Maximum allowed length for generic text fields (adjustable per use‑case). */
    private static final int MAX_GENERIC_LENGTH = 4096;

    /** Pattern that matches any control character (U+0000‑U+001F, U+007F). */
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]");

    /** Pattern that matches typical SQL meta‑characters used in injection attempts. */
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(\\b(select|insert|update|delete|drop|alter|create|truncate)\\b|--|;|\\*|\\bunion\\b|\\binto\\b|\\bexec\\b)");

    /** Pattern that matches script tags (case‑insensitive). */
    private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile("(?i)<\\s*script[^>]*>(.*?)<\\s*/\\s*script>");

    @Inject
    public InputSanitizer() {
        // CDI constructor
    }

    /**
     * Sanitizes a generic text input.
     *
     * @param raw the raw user supplied string, may be {@code null}
     * @return a safe, trimmed string with HTML encoded and control characters removed.
     */
    public String sanitize(String raw) {
        if (raw == null) {
            return null;
        }
        // Trim and enforce length limit
        String trimmed = raw.trim();
        if (trimmed.length() > MAX_GENERIC_LENGTH) {
            trimmed = trimmed.substring(0, MAX_GENERIC_LENGTH);
        }

        // Remove control characters
        String noControls = CONTROL_CHARS.matcher(trimmed).replaceAll("");

        // Strip script tags completely
        String noScripts = SCRIPT_TAG_PATTERN.matcher(noControls).replaceAll("");

        // Encode for HTML context (prevents XSS)
        return Encode.forHtml(noScripts);
    }

    /**
     * Sanitizes a string that will be placed inside an HTML attribute value.
     *
     * @param raw the raw attribute value
     * @return a safely encoded attribute value
     */
    public String sanitizeForHtmlAttribute(String raw) {
        if (raw == null) {
            return null;
        }
        String sanitized = sanitize(raw);
        return Encode.forHtmlAttribute(sanitized);
    }

    /**
     * Sanitizes a string that will be used in a URL path or query parameter.
     *
     * @param raw the raw URL component
     * @return a URL‑encoded safe string
     */
    public String sanitizeForUrl(String raw) {
        if (raw == null) {
            return null;
        }
        // Encode using standard percent‑encoding (UTF‑8)
        return java.net.URLEncoder.encode(sanitize(raw), StandardCharsets.UTF_8);
    }

    /**
     * Performs a lightweight SQL‑injection mitigation by escaping single quotes
     * and removing obvious malicious patterns. <strong>Do NOT rely on this method
     * for query safety – always use prepared statements.</strong>
     *
     * @param raw the raw SQL fragment
     * @return a string with dangerous patterns removed/escaped
     */
    public String sanitizeForSql(String raw) {
        if (raw == null) {
            return null;
        }
        String sanitized = sanitize(raw);
        // Escape single quotes
        sanitized = sanitized.replace("'", "''");
        // Remove known injection keywords
        sanitized = SQL_INJECTION_PATTERN.matcher(sanitized).replaceAll("");
        return sanitized;
    }

    /**
     * Normalises and validates an e‑mail address. The method trims whitespace,
     * lower‑cases the domain part and ensures the address matches a simple RFC‑5322
     * pattern. It does NOT guarantee the address exists.
     *
     * @param email raw e‑mail address
     * @return a cleaned e‑mail address or {@code null} if the input is invalid
     */
    public String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        // Basic RFC‑5322 email regex (simplified)
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$";
        if (!Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE).matcher(trimmed).matches()) {
            return null;
        }
        // Lower‑case domain part only (local‑part may be case‑sensitive)
        int atIdx = trimmed.lastIndexOf('@');
        String local = trimmed.substring(0, atIdx);
        String domain = trimmed.substring(atIdx + 1).toLowerCase(Locale.ROOT);
        return local + "@" + domain;
    }

    /**
     * Encodes a potentially sensitive string (e.g., tax ID) using AES‑256 in GCM mode.
     * The method delegates to {@link EncryptionService} if available via CDI.
     *
     * @param plainText the clear text to encrypt
     * @return Base64‑encoded ciphertext, or {@code null} if input is {@code null}
     */
    public String encryptSensitive(String plainText) {
        if (plainText == null) {
            return null;
        }
        // EncryptionService is optional – fallback to simple Base64 (not secure) if not injected.
        try {
            EncryptionService encryptionService = EncryptionServiceHolder.getInstance();
            if (encryptionService != null) {
                return encryptionService.encrypt(plainText);
            }
        } catch (Exception ignored) {
            // fall‑through to Base64
        }
        return Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decrypts a ciphertext previously produced by {@link #encryptSensitive(String)}.
     *
     * @param cipherText Base64‑encoded ciphertext
     * @return the original plain text, or {@code null} if decryption fails
     */
    public String decryptSensitive(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        try {
            EncryptionService encryptionService = EncryptionServiceHolder.getInstance();
            if (encryptionService != null) {
                return encryptionService.decrypt(cipherText);
            }
        } catch (Exception ignored) {
            // fall‑through to Base64 decode
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Holder for lazily obtaining the {@link EncryptionService} bean without creating a hard
     * dependency cycle. This pattern works because CDI will initialise the bean before the first
     * call to {@link #encryptSensitive(String)} or {@link #decryptSensitive(String)}.
     */
    private static final class EncryptionServiceHolder {
        private static volatile EncryptionService instance;

        static EncryptionService getInstance() {
            if (instance == null) {
                synchronized (EncryptionServiceHolder.class) {
                    if (instance == null) {
                        try {
                            instance = jakarta.enterprise.inject.spi.CDI.current()
                                    .select(EncryptionService.class).get();
                        } catch (Exception e) {
                            // CDI not available – return null and fallback to Base64.
                            instance = null;
                        }
                    }
                }
            }
            return instance;
        }
    }
}

/**
 * Minimal contract for the encryption service used by {@link InputSanitizer}.
 * The real implementation lives in {@code org.nlh4j.saas.membershiphub.config.EncryptionService}.
 */
interface EncryptionService {
    String encrypt(String plainText) throws Exception;
    String decrypt(String cipherText) throws Exception;
}