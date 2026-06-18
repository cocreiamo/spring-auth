package com.cocreiamo.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point of the spring-auth starter: the Laravel-Fortify analog for Spring.
 *
 * <p>spring-auth sits ON TOP of Spring Security. This auto-configuration registers sane
 * Spring Security defaults (form login, session, CSRF, {@code DelegatingPasswordEncoder},
 * session-fixation {@code changeSessionId}, logout, opt-in remember-me) and the flows
 * Spring Security lacks in core (password reset, email verification, password-confirm /
 * sudo window, login throttling), exposed over a clean user-provider SPI. Every bean is
 * {@code @ConditionalOnMissingBean} and every flow is gated by a
 * {@code spring-auth.features.*} {@code @ConditionalOnProperty} toggle, so an adopter
 * overrides any single piece or drops to raw {@code HttpSecurity}.
 *
 * <p>The auto-config backs off entirely when the adopter declares their own
 * {@code SecurityFilterChain} bean (the documented escape hatch,
 * {@code @ConditionalOnMissingBean(SecurityFilterChain.class)}): the config is a
 * convenience over {@code HttpSecurity}, never a wall in front of it.
 *
 * <p>PREDISPOSITION SKELETON. This class is intentionally an empty placeholder: it boots
 * the context and pins the package + toggle, nothing more. The actual flows are tracked as
 * GitHub issues (Epics A and B) and will be added bean-by-bean under contract-first TDD.
 * Replacing this placeholder with the real beans is the work of Epic A, story A1.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring-auth", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SpringAuthProperties.class)
public class SpringAuthAutoConfiguration {
    // Beans are added per Epic A / Epic B story. See docs/DESIGN.md and the issue backlog.
}
