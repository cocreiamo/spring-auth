package com.cocreiamo.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration for the spring-auth starter (the Laravel {@code config/auth.php} +
 * {@code config/fortify.php} analog), mapping onto Spring Security building blocks 1:1
 * without replacing them.
 *
 * <p>PREDISPOSITION SKELETON. Only the master {@code enabled} switch exists today. The full
 * surface (guard / provider / password-reset / password-confirm / remember-me / features.*)
 * is specified in {@code docs/DESIGN.md} section 3.3 and is filled in by Epic A, story A4
 * ({@code spring-auth.features.*} model + IDE metadata).
 */
@ConfigurationProperties(prefix = "spring-auth")
public class SpringAuthProperties {

    /**
     * Master switch. When false, the starter registers no SecurityFilterChain, no flow, and
     * no REST endpoint, leaving the application on raw Spring Security defaults.
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
