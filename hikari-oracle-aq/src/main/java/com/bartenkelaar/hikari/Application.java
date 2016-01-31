package com.bartenkelaar.hikari;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
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

        LOG.info("Sending 4 messages");
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        String queueName = context.getEnvironment().getProperty("database.queue.owner") + ".TEST_QUEUE";
        for (int i = 1; i <= 4; i++) {
            LOG.debug("Sending message " + i);
            jmsTemplate.convertAndSend(queueName, Integer.toString(i));
        }

        LOG.info("Waiting until error occurs");
        while (!errorOccured.get()) { /* wait */ }

        LOG.info("Closing application context");
        context.close();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer configurer() {
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
