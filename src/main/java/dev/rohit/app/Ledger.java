package dev.rohit.app;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
public interface Ledger extends JpaRepository<LedgerEntry,Long>{Page<LedgerEntry> findByAccountIdOrderByIdDesc(Long accountId,Pageable page);}
