package com.xingcanai.csqe.auditing.entity;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesSpeechAnalysisMigrationTest {

    @Test
    void createsTableAndEnforcesStatusAndVersionUniqueness() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:sales_speech_migration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");

        try (Connection connection = dataSource.getConnection()) {
            String migration = new ClassPathResource(
                    "db/migration/V20260723001__create_sales_speech_analysis.sql")
                    .getContentAsString(StandardCharsets.UTF_8)
                    .replace("timestamptz", "timestamp with time zone");
            try (Statement statement = connection.createStatement()) {
                statement.execute(migration);
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("""
                     select count(*)
                     from information_schema.tables
                     where table_name = 'XCA_SALES_SPEECH_ANALYSIS'
                     """)) {
                resultSet.next();
                assertEquals(1, resultSet.getInt(1));
            }

            executeInsert(connection, "analysis-1", "PROCESSING", "2026-07-18");
            assertThrows(
                    SQLException.class,
                    () -> executeInsert(connection, "analysis-2", "COMPLETED", "2026-07-18"));
            assertThrows(
                    SQLException.class,
                    () -> executeInsert(connection, "analysis-3", "INVALID", "2026-07-19"));
        }
    }

    private void executeInsert(
            Connection connection,
            String id,
            String status,
            String evalPeriod) throws SQLException {
        try (var statement = connection.prepareStatement("""
            insert into xca_sales_speech_analysis (
                id,
                employee_id,
                employee_qw_id,
                eval_period,
                period_start_time,
                period_end_time,
                status,
                prompt_version,
                requested_by
            ) values (?, 'employee-1', 'qw-1', ?, now(), now(), ?, 'v1', 'tester')
            """)) {
            statement.setString(1, id);
            statement.setObject(2, java.time.LocalDate.parse(evalPeriod));
            statement.setString(3, status);
            statement.executeUpdate();
        }
    }
}
