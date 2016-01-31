package com.bartenkelaar.hikari;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Listener that picks up messages from the TEST_QUEUE and inserts them into the TEST_TABLE
 */
@Component
public class TestListener {
    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);

    private final DataSource dataSource;

    @Inject
    public TestListener(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @JmsListener(destination = "${database.queue.owner}.TEST_QUEUE", concurrency = "1")
    @Transactional
    public void handleMessage(String message) throws SQLException {
        LOG.debug("Handling message '{}'", message);
        dataSource.getConnection().createStatement().execute("insert into test_table values (" + message + ")");
    }
}
