package com.jpmc.midascore;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

import com.jpmc.midascore.foundation.Transaction;

@Configuration
@EnableKafka
@Profile("kafka") 
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Transaction> kafkaListenerContainerFactory() {

        JsonDeserializer<Transaction> deserializer = new JsonDeserializer<>(Transaction.class);
        deserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", deserializer);

        DefaultKafkaConsumerFactory<String, Transaction> factory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);

        ConcurrentKafkaListenerContainerFactory<String, Transaction> container =
                new ConcurrentKafkaListenerContainerFactory<>();
        container.setConsumerFactory(factory);

        return container;
    }
}
