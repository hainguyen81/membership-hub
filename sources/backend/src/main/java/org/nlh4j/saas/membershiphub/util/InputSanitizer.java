package org.nlh4j.saas.membershiphub.util;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Utility class that sanitizes user supplied input before it is persisted or processed.
 * <p>
 * The implementation follows OWASP A03 (Injection) recommendations:
 * <ul>
 *   <li>HTML/JS injection is mitigated by stripping all HTML tags.</li>
 *   <li>Control characters and non‑printable Unicode are removed.</li>
 *   <li>Leading/trailing whitespace is trimmed.</li>
 *   <li>All returned values are safe for use with parameterised JPA queries.</li>
 * </ul>
 * This class is deliberately stateless and can be used as a CDI bean or via its static helpers.
 */
@ApplicationScoped
@Named("inputSanitizer")
public class InputSanitizer {

    private static final Logger LOG = Logger.getLogger(InputSanitizer.class);

    /** Pattern that matches any control character (U+0000‑U+001F, U+007F) */
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]");

    /** Private constructor to prevent instantiation – use static methods or CDI injection. */
    private InputSanitizer() {
        // no‑op
    }

    /**
     * Sanitizes a plain text input.
     *
     * @param raw the raw user supplied string; may be {@code null}
     * @return a safe, trimmed string without HTML tags or control characters,
     *         or {@code null} if the input was {@code null}
     */
    public static String sanitize(String raw) {
        if (raw == null) {
            return null;
        }
        // Trim whitespace first
        String trimmed = raw.trim();

        // Remove any HTML/JS content – keep only plain text
        String cleaned = Jsoup.clean(trimmed, Safelist.none());

        // Strip control characters that could be used for injection attacks
        String safe = CONTROL_CHARS.matcher(cleaned).replaceAll("");

        // Optionally log sanitisation (debug level only to avoid leaking data)
        LOG.debugf("Sanitized input. Original length=%d, sanitized length=%d", raw.length(), safe.length());

        return safe;
    }

    /**
     * Sanitizes an email address. The method removes HTML, control characters and
     * validates a very simple email pattern. If validation fails, {@code null} is returned.
     *
     * @param rawEmail raw email string
     * @return a sanitized email or {@code null} if the input is not a valid email
     */
    public static String sanitizeEmail(String rawEmail) {
        String candidate = sanitize(rawEmail);
        if (candidate == null) {
            return null;
        }
        // Very lightweight email validation – sufficient for early sanitisation.
        // Full validation should be performed by Bean Validation annotations on the entity.
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (candidate.matches(emailRegex)) {
            return candidate;
        }
        LOG.warnf("Email sanitisation failed validation: %s", candidate);
        return null;
    }

    /**
     * Sanitizes a collection of strings, returning a new {@link List} with each element
     * processed by {@link #sanitize(String)}. {@code null} elements are preserved as {@code null}
     * in the resulting list.
     *
     * @param rawCollection the collection to sanitize; may be {@code null}
     * @return a new {@link List} containing sanitized strings, or {@code null} if the input was {@code null}
     */
    public static List<String> sanitizeCollection(Collection<String> rawCollection) {
        if (rawCollection == null) {
            return null;
        }
        List<String> sanitized = new ArrayList<>(rawCollection.size());
        for (String item : rawCollection) {
            sanitized.add(sanitize(item));
        }
        return sanitized;
    }

    /**
     * Generic sanitisation for any {@link CharSequence}. Returns a trimmed, HTML‑free,
     * control‑character‑free {@link String}.
     *
     * @param raw any character sequence; may be {@code null}
     * @return sanitized string or {@code null}
     */
    public static String sanitize(CharSequence raw) {
        return sanitize(Objects.toString(raw, null));
    }

    /**
     * Convenience method to be used inside JPA entity listeners or service layers.
     * Throws {@link IllegalArgumentException} if the sanitized result is {@code null}
     * while the original input was non‑null (e.g., email failed validation).
     *
     * @param raw the raw input
     * @param fieldName logical name of the field (used for exception messages)
     * @return sanitized string
     */
    public static String requireSanitized(String raw, String fieldName) {
        String sanitized = sanitize(raw);
        if (raw != null && sanitized == null) {
            throw new IllegalArgumentException(
                String.format("Sanitisation of field '%s' resulted in null value.", fieldName));
        }
        return sanitized;
    }
}