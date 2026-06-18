package com.cocreiamo.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test for the predisposition skeleton: the auto-configuration loads, binds its
 * properties, and respects the master switch. As the real beans land (Epic A / Epic B),
 * this class grows the wiring + back-off assertions (e.g. an adopter SecurityFilterChain
 * disables the auto-config, story A5).
 */
class SpringAuthAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SpringAuthAutoConfiguration.class));

    /**
     * @spec.given the spring-auth auto-configuration on the classpath
     * @spec.when  a Spring application context starts with defaults
     * @spec.then  the auto-config is active and SpringAuthProperties is bound
     */
    @Test
    void loadsAndBindsPropertiesByDefault() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(SpringAuthAutoConfiguration.class);
            assertThat(ctx).hasSingleBean(SpringAuthProperties.class);
            assertThat(ctx.getBean(SpringAuthProperties.class).isEnabled()).isTrue();
        });
    }

    /**
     * @spec.given the spring-auth auto-configuration on the classpath
     * @spec.when  the context starts with spring-auth.enabled=false
     * @spec.then  the auto-config backs off and registers no SpringAuth beans
     */
    @Test
    void backsOffWhenDisabled() {
        runner.withPropertyValues("spring-auth.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(SpringAuthAutoConfiguration.class));
    }
}
