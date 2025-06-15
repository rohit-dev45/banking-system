package com.example.banking;

import com.example.banking.model.Transaction;
import com.example.banking.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    private TransactionService service;

    @Test
    public void testSaveAndRetrieveTransaction() {
        Transaction tx = new Transaction();
        tx.setType("DEPOSIT");
        tx.setAmount(1000.0);
        tx.setAccountNumber("12345678");
        tx.setTimestamp(LocalDateTime.now());

        Transaction saved = service.save(tx);
        Transaction fetched = service.getById(saved.getId());

        assertEquals(saved.getAmount(), fetched.getAmount());
    }
}