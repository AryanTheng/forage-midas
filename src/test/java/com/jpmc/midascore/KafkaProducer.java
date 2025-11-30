package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    private final String topic;
    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    public KafkaProducer(
            @Value("${kafka.topic.name}") String topic,
            KafkaTemplate<String, Transaction> kafkaTemplate
    ) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String transactionLine) {
        String[] t = transactionLine.split(", ");
        Transaction tx = new Transaction(
                Long.parseLong(t[0]),
                Long.parseLong(t[1]),
                Float.parseFloat(t[2])
        );

        kafkaTemplate.send(topic, tx);
    }
}
