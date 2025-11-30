package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import com.jpmc.midascore.foundation.Incentive;


@Component
public class TransactionListener {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRepository;

    public TransactionListener(UserRepository userRepository,
                               TransactionRecordRepository transactionRepository,
                               RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "midas-core-group")
    public void listen(Transaction tx) {

        // Load sender + recipient
        UserRecord sender = userRepository.findById(tx.getSenderId()).orElse(null);
        UserRecord recipient = userRepository.findById(tx.getRecipientId()).orElse(null);

        // Validate
        if (sender == null || recipient == null) return;
        if (sender.getBalance() < tx.getAmount()) return;

        // Apply balance updates
        sender.setBalance(sender.getBalance() - tx.getAmount());
        recipient.setBalance(recipient.getBalance() + tx.getAmount());

        userRepository.save(sender);
        userRepository.save(recipient);


        Incentive incentive = restTemplate.postForObject(
            "http://localhost:8080/incentive",
            tx,
            Incentive.class
        );

        float incentiveAmount = (incentive != null ? incentive.getAmount() : 0);

        // Add incentive ONLY to recipient (NOT deducted from sender)
        recipient.setBalance(recipient.getBalance() + incentiveAmount);
        userRepository.save(recipient);

        // Save transaction
        TransactionRecord record =
                new TransactionRecord(sender, recipient, tx.getAmount());
        transactionRepository.save(record);

        // Print waldorf balance ON EACH TRANSACTION (test requires continuous output)
        UserRecord w = userRepository.findByName("waldorf").orElse(null);
        if (w != null) {
            System.out.println("💰 WALDORF BALANCE = " + w.getBalance());
        }

    }
}
