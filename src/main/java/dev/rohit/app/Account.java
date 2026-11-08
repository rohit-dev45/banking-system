package dev.rohit.app;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity public class Account {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Long ownerId;
 @Column(nullable=false,precision=19,scale=2) public BigDecimal balance=BigDecimal.ZERO;
 @Version public long version;
 protected Account(){} Account(Long owner){ownerId=owner;}
}
