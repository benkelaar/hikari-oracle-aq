package com.bartenkelaar.hikari;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * singleton to communicate whether the program should keep running.
 *
 * finishes when an error occurs or the last message has been checked.
 */
@Component
@Singleton
class Finisher implements ErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final int amountOfMessages;
    private volatile boolean finished;

    @Inject
    Finisher(@Value("${test.messages.amount}") int amountOfMessages) {
        this.amountOfMessages = amountOfMessages;
    }

    boolean isFinished() {
        return finished;
    }

    void checkFinished(int messageNumber) {
        boolean lastMessage = messageNumber == amountOfMessages;
        if (lastMessage) {
            LOG.info("Last message was correctly processed.");
        }
        finished = finished || lastMessage;
    }

    @Override
    public void handleError(Throwable t) {
        LOG.error("Exception caught in error handler", t);
        LOG.warn("Finishing program");
        finished = true;
    }
}
