package com.example.banking.service;

import com.example.banking.model.Transaction;
import com.example.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository repo;

    private final Map<Long, Transaction> transactionCache = new HashMap<>();

    public Transaction save(Transaction tx) {
        Transaction saved = repo.save(tx);
        transactionCache.put(saved.getId(), saved);
        return saved;
    }

    public Transaction getById(Long id) {
        return transactionCache.getOrDefault(id, repo.findById(id).orElse(null));
    }

    public List<Transaction> getAll() {
        return repo.findAll();
    }
}