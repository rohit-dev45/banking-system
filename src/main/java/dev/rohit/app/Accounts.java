package dev.rohit.app;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
public interface Accounts extends JpaRepository<Account,Long> {
 List<Account> findByOwnerId(Long ownerId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from Account a where a.id = :id") Optional<Account> lock(Long id);
}
