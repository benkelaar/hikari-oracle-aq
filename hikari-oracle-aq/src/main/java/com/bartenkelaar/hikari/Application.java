package com.bartenkelaar.hikari;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.ErrorHandler;

import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@ComponentScan
@PropertySource("classpath:hikari.properties")
public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private static AtomicBoolean errorOccured = new AtomicBoolean();

    public static void main(String[] args) {
        LOG.info("Loading application context");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class);

        Environment environment = context.getEnvironment();
        sendMessages(context.getBean(JmsTemplate.class),
                environment.getProperty("database.queue.owner") + ".TEST_QUEUE",
                environment.getProperty("test.messages.amount", Integer.class)
        );

        LOG.info("Waiting until error occurs");
        while (!errorOccured.get()) { /* wait */ }

        LOG.info("Closing application context");
        context.close();
    }

    private static void sendMessages(JmsTemplate jmsTemplate, String queueName, int amountOfMessages) {
        LOG.info("Sending {} messages to {}", amountOfMessages, queueName);
        for (int i = 1; i <= amountOfMessages; i++) {
            LOG.debug("Sending message " + i);
            jmsTemplate.convertAndSend(queueName, Integer.toString(i));
        }
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer configurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ErrorHandler programCloser() {
        return t -> {
            LOG.error("Exception caught in error handler", t);
            errorOccured.set(true);
        };
    }
}
