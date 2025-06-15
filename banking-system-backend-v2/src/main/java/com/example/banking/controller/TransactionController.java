package com.example.banking.controller;

import com.example.banking.model.Transaction;
import com.example.banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService service;

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction tx) {
        return ResponseEntity.ok(service.save(tx));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> get(@PathVariable Long id) {
        Transaction tx = service.getById(id);
        return tx != null ? ResponseEntity.ok(tx) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Transaction> all() {
        return service.getAll();
    }
}