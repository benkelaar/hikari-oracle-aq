package com.bartenkelaar.hikari;

import oracle.jms.AQjmsFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.util.ErrorHandler;

import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.sql.DataSource;

/**
 * Configuration for JMS
 */
@Configuration
@EnableJms
public class JmsConfig {
    @Bean
    public DestinationResolver destinationResolver() {
        return new DynamicDestinationResolver();
    }

    @Bean
    public QueueConnectionFactory jmsQConnectionFactory(DataSource dataSource) throws JMSException {
        return AQjmsFactory.getQueueConnectionFactory(dataSource);
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(QueueConnectionFactory connectionFactory,
                                                                          DestinationResolver resolver,
                                                                          ErrorHandler errorHandler) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionTransacted(true);
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(resolver);
        factory.setErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate(QueueConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setConnectionFactory(connectionFactory);
        return jmsTemplate;
    }
}
