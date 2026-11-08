package dev.rohit.app;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity public class LedgerEntry {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Long accountId;
 @Column(nullable=false,precision=19,scale=2) public BigDecimal amount;
 @Column(nullable=false) public String kind;
 @Column(nullable=false) public Instant createdAt=Instant.now();
 protected LedgerEntry(){} LedgerEntry(Long account,BigDecimal amount,String kind){accountId=account;this.amount=amount;this.kind=kind;}
}
