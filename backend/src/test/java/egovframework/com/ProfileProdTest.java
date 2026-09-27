package egovframework.com;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileProdTest {

    @Test
    void profileLoadsExpectedSettingsWithoutConnectingToDatabase() {
        new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=prod",
                "FLYWAY_BASELINE_ON_MIGRATE=false",
                "ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN=true")
            .run(context -> {
                var env = context.getEnvironment();
                assertThat(context).hasNotFailed().doesNotHaveBean(javax.sql.DataSource.class);
                assertThat(env.getProperty("Globals.DbType")).isEqualTo("postgresql");
                assertThat(env.getProperty("Globals.OsType")).isEqualTo("LINUX");
                assertThat(env.getProperty("logging.rollingpolicy.maxHistory")).isEqualTo("90");
            assertThat(env.getProperty("spring.sql.init.mode")).isEqualTo("never");
            assertThat(env.getProperty("spring.flyway.enabled")).isEqualTo("true");
            assertThat(env.getProperty("spring.flyway.baseline-on-migrate")).isEqualTo("false");
            assertThat(env.getProperty("spring.flyway.baseline-version")).isEqualTo("0");
            assertThat(env.getProperty("Healthcenter.AccountRecovery.ExposeDevelopmentResetToken"))
                .isEqualTo("false");
            });
    }
}
