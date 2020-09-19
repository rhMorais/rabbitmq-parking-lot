package br.com.facef.rabbitmqdlq.consumer;

import br.com.facef.rabbitmqdlq.configuration.DirectExchangeConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MessageConsumer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = DirectExchangeConfiguration.ORDER_MESSAGES_QUEUE_NAME)
    public void processOrderMessage(Message message) {
        log.info("PROCESSING MESSAGE: {}", message.toString());
        // By default the messages will be requeued
        throw new RuntimeException("Business Rule Exception");
    }

    @RabbitListener(queues = DirectExchangeConfiguration.QUEUE_MESSAGES_DLQ)
    public void processOrderMessageDlq(Message message) {
        log.info("PROCESSING DLQ MESSAGE: {}", message.toString());

        Integer retriesCount = (Integer) message.getMessageProperties().getHeaders()
                                    .get(DirectExchangeConfiguration.X_RETRIES_HEADER);
        if (retriesCount == null)
            retriesCount = Integer.valueOf(0);

        if (retriesCount > DirectExchangeConfiguration.MAX_RETRIES) {
            log.info("SENDING MESSAGE TO THE PARKING LOT QUEUE");
            this.rabbitTemplate.send(DirectExchangeConfiguration.EXCHANGE_NAME_PARKING_LOT,
                    message.getMessageProperties().getReceivedRoutingKey(), message);
            return;
        }

        // Dessa maneira faz o retrying manualmente
        log.info("RETRYING MESSAGE FOR THE {} TIME", retriesCount);
        message.getMessageProperties().getHeaders().put(DirectExchangeConfiguration.X_RETRIES_HEADER, ++retriesCount);
        this.rabbitTemplate.send(DirectExchangeConfiguration.DIRECT_EXCHANGE_NAME,
            message.getMessageProperties().getReceivedRoutingKey(), message);
    }

    @RabbitListener(queues = DirectExchangeConfiguration.PARKING_LOT_QUEUE)
    public void processParkingLotQueue(Message failedMessage) {
        log.info("RECEIVED MESSAGE IN PARKING LOT QUEUE: {}", failedMessage.toString());
        // Save to DB or send a notification.
        // custei fazer funcionar so o encaminhamento kkk
    }

}