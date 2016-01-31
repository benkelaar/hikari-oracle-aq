package com.bartenkelaar.hikari;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Listener that picks up messages from the TEST_QUEUE and inserts them into the TEST_TABLE
 */
@Component
public class TestListener {
    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);

    private final DataSource dataSource;
    private final Finisher finisher;

    @Inject
    public TestListener(DataSource dataSource, Finisher finisher) {
        this.dataSource = dataSource;
        this.finisher = finisher;
    }

    @JmsListener(destination = "${database.queue.owner}.TEST_QUEUE", concurrency = "2")
    @Transactional
    public void handleMessage(String message) throws SQLException {
        LOG.debug("Handling message '{}'", message);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("insert into test_table values (" + message + ")");
        }
        finally {
            finisher.checkFinished(Integer.valueOf(message));
        }
    }
}
