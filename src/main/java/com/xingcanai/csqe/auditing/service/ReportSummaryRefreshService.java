package com.xingcanai.csqe.auditing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReportSummaryRefreshService {

    private static final Logger logger = LoggerFactory.getLogger(ReportSummaryRefreshService.class);
    private static final long REFRESH_LOCK_ID = 20260629001L;

    private final JdbcTemplate jdbcTemplate;

    public ReportSummaryRefreshService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReportSummaryRefreshResult refresh() {
        Instant startedAt = Instant.now();
        try {
            return jdbcTemplate.execute((ConnectionCallback<ReportSummaryRefreshResult>) connection ->
                    refreshWithConnection(connection, startedAt));
        } catch (DataAccessException e) {
            logger.error("Failed to refresh report_summary_mv", e);
            return result(ReportSummaryRefreshStatus.FAILED, startedAt);
        }
    }

    private ReportSummaryRefreshResult refreshWithConnection(Connection connection, Instant startedAt) {
        boolean locked = false;
        try {
            locked = tryLock(connection);
            if (!locked) {
                logger.info("Skip report_summary_mv refresh because another refresh is running");
                return result(ReportSummaryRefreshStatus.SKIPPED, startedAt);
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY report_summary_mv");
            }

            ReportSummaryRefreshResult result = result(ReportSummaryRefreshStatus.SUCCESS, startedAt);
            logger.info("Refreshed report_summary_mv in {} ms", result.durationMs());
            return result;
        } catch (SQLException e) {
            logger.error("Failed to refresh report_summary_mv", e);
            return result(ReportSummaryRefreshStatus.FAILED, startedAt);
        } finally {
            if (locked) {
                unlock(connection);
            }
        }
    }

    private boolean tryLock(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT pg_try_advisory_lock(?)")) {
            statement.setLong(1, REFRESH_LOCK_ID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        }
    }

    private void unlock(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT pg_advisory_unlock(?)")) {
            statement.setLong(1, REFRESH_LOCK_ID);
            statement.executeQuery();
        } catch (SQLException e) {
            logger.warn("Failed to unlock report_summary_mv refresh advisory lock", e);
        }
    }

    private ReportSummaryRefreshResult result(ReportSummaryRefreshStatus status, Instant startedAt) {
        Instant finishedAt = Instant.now();
        return new ReportSummaryRefreshResult(status, Duration.between(startedAt, finishedAt).toMillis(), finishedAt);
    }
}
