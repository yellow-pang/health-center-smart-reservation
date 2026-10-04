package egovframework.healthcenter.migration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Runs only with -Pmigration-test against an explicitly supplied disposable *_ci database. */
class PostgresqlMigrationIT {

    private String url;
    private String username;
    private String password;
    private String schema;

    @BeforeEach
    void createIsolatedSchema() throws SQLException {
        url = requiredEnvironment("TEST_DATABASE_URL");
        username = requiredEnvironment("TEST_DATABASE_USERNAME");
        password = requiredEnvironment("TEST_DATABASE_PASSWORD");
        if (!url.matches("jdbc:postgresql://[^/]+/[A-Za-z0-9_]+_ci(?:\\?.*)?")) {
            throw new IllegalArgumentException("Migration tests require a disposable PostgreSQL database ending in _ci");
        }
        schema = "migration_test_" + UUID.randomUUID().toString().replace("-", "");
        try (var connection = DriverManager.getConnection(url, username, password);
             var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
        }
    }

    @AfterEach
    void dropOnlyThisTestsSchema() throws SQLException {
        if (schema != null) {
            try (var connection = DriverManager.getConnection(url, username, password);
                 var statement = connection.createStatement()) {
                statement.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
            }
        }
    }

    @Test
    void emptyDatabaseMigratesOnceWithoutCreatingDemoAccountsOrBusinessData() throws SQLException {
        var flyway = flyway(false);
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        flyway.validate();

        assertThat(query("SELECT count(*) FROM common_code_groups")).isEqualTo("6");
        assertThat(query("SELECT count(*) FROM common_codes")).isEqualTo("27");
        for (String table : new String[] {"health_centers", "members", "service_types", "reservations", "visits", "queue_tickets"}) {
            assertThat(query("SELECT count(*) FROM " + table)).as(table).isEqualTo("0");
        }
        assertThat(query("SELECT count(*) FROM flyway_schema_history WHERE success AND type = 'SQL'"))
            .isEqualTo("2");
    }

    @Test
    void legacyDatabaseRequiresExplicitAdoptionAndPreservesOperationalData() throws SQLException {
        try (var connection = connection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/postgresql/schema.sql"));
            try (var statement = connection.createStatement()) {
                statement.execute("""
                    INSERT INTO health_centers (id, name) VALUES (1, 'Existing center');
                    INSERT INTO members (id, health_center_id, email, password, name, phone, role, active)
                    VALUES (1, 1, 'admin@test.com', 'existing-password-hash', 'Existing admin', '01000000000', 'ADMIN', false);
                    INSERT INTO service_types (id, health_center_id, code, name, default_capacity)
                    VALUES (1, 1, 'VACCINATION', 'Existing service', 12);
                    INSERT INTO reservation_slots (id, health_center_id, service_type_id, slot_date, start_time, end_time)
                    VALUES (1, 1, 1, DATE '2026-01-01', TIME '09:00', TIME '10:00');
                    INSERT INTO reservations (reservation_no, health_center_id, member_id, service_type_id,
                        reservation_slot_id, visitor_name, visitor_phone, status)
                    VALUES ('RSV-SWAGGER-CHECKIN-001', 1, 1, 1, 1, 'Existing citizen', '01000000001', 'COMPLETED');
                    INSERT INTO common_code_groups (id, group_code, group_name)
                    VALUES (1, 'USER_ROLE', 'Custom role names');
                    INSERT INTO common_codes (group_id, code, code_name, active)
                    VALUES (1, 'CITIZEN', 'Custom citizen name', false);
                    SELECT setval(pg_get_serial_sequence('common_code_groups', 'id'), 1);
                    """);
            }
        }

        assertThatThrownBy(() -> flyway(false).migrate()).isInstanceOf(FlywayException.class);
        assertThat(flyway(true).migrate().migrationsExecuted).isEqualTo(2);
        assertThat(flyway(false).migrate().migrationsExecuted).isZero();
        flyway(false).validate();

        assertThat(query("SELECT name FROM health_centers WHERE id = 1")).isEqualTo("Existing center");
        assertThat(query("SELECT password FROM members WHERE id = 1")).isEqualTo("existing-password-hash");
        assertThat(query("SELECT active FROM members WHERE id = 1")).isEqualTo("f");
        assertThat(query("SELECT count(*) FROM members")).isEqualTo("1");
        assertThat(query("SELECT default_capacity FROM service_types WHERE id = 1")).isEqualTo("12");
        assertThat(query("SELECT status FROM reservations WHERE reservation_no = 'RSV-SWAGGER-CHECKIN-001'"))
            .isEqualTo("COMPLETED");
        assertThat(query("SELECT code_name FROM common_codes WHERE group_id = 1 AND code = 'CITIZEN'"))
            .isEqualTo("Custom citizen name");
        assertThat(query("SELECT active FROM common_codes WHERE group_id = 1 AND code = 'CITIZEN'"))
            .isEqualTo("f");
        assertThat(query("SELECT version FROM flyway_schema_history WHERE type = 'BASELINE'"))
            .isEqualTo("0");
    }

    private Flyway flyway(boolean baseline) {
        return Flyway.configure()
            .dataSource(url, username, password)
            .schemas(schema)
            .defaultSchema(schema)
            .createSchemas(false)
            .locations("classpath:db/migration")
            .baselineOnMigrate(baseline)
            .baselineVersion("0")
            .cleanDisabled(true)
            .load();
    }

    private Connection connection() throws SQLException {
        var connection = DriverManager.getConnection(url, username, password);
        connection.setSchema(schema);
        return connection;
    }

    private String query(String sql) throws SQLException {
        try (var connection = connection();
             var statement = connection.createStatement();
             var result = statement.executeQuery(sql)) {
            assertThat(result.next()).isTrue();
            return result.getString(1);
        }
    }

    private static String requiredEnvironment(String name) {
        var value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required for -Pmigration-test; never use the application database");
        }
        return value;
    }
}
