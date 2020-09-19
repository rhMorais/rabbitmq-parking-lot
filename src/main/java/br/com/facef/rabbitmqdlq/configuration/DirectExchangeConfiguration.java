package br.com.facef.rabbitmqdlq.configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectExchangeConfiguration {

    public static final String DIRECT_EXCHANGE_NAME = "order-exchange";
    public static final String ORDER_MESSAGES_QUEUE_NAME = "order-messages-queue";

    public static final String DLX_EXCHANGE_MESSAGES = DIRECT_EXCHANGE_NAME + ".dlx";
    public static final String QUEUE_MESSAGES_DLQ = ORDER_MESSAGES_QUEUE_NAME + ".dlq";

    public static final String EXCHANGE_NAME_PARKING_LOT = DIRECT_EXCHANGE_NAME + ".parkingLot";
    public static final String PARKING_LOT_QUEUE = ORDER_MESSAGES_QUEUE_NAME + ".parkingLot";

    public static final String X_RETRIES_HEADER = "x-retries";
    public static final Byte MAX_RETRIES = 2;

    @Bean
    Queue orderMessagesQueue() {
        return QueueBuilder.durable(ORDER_MESSAGES_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_MESSAGES)
                .build();
    }


    @Bean
    DirectExchange exchange() {
        return ExchangeBuilder.directExchange(DIRECT_EXCHANGE_NAME).durable(true).build();
    }

    @Bean
    Binding bindingOrderMessagesQueue(@Qualifier("orderMessagesQueue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ORDER_MESSAGES_QUEUE_NAME);
    }


    @Bean
    FanoutExchange deadLetterExchange() {
        return new FanoutExchange(DLX_EXCHANGE_MESSAGES);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_MESSAGES_DLQ).build();
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }


    @Bean
    FanoutExchange parkingLotExchange() {
        return new FanoutExchange(EXCHANGE_NAME_PARKING_LOT);
    }

    @Bean
    Queue parkingLotQueue() {
        return QueueBuilder.durable(PARKING_LOT_QUEUE).build();
    }

    @Bean
    Binding parkingLotBinding() {
        return BindingBuilder.bind(parkingLotQueue()).to(parkingLotExchange());
    }
}