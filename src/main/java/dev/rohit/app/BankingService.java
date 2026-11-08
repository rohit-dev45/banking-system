package dev.rohit.app;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Service
public class BankingService {
 private final Accounts accounts; private final Ledger ledger;
 BankingService(Accounts a,Ledger l){accounts=a;ledger=l;}
 static void amount(BigDecimal value){if(value==null||value.signum()<=0||value.scale()>2||value.precision()>16)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Positive amount with at most two decimal places required");}
 Account owned(Account a,Long owner){if(!a.ownerId.equals(owner))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found");return a;}
 private Account locked(Long id){return accounts.lock(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found"));}
 @Transactional public Account deposit(Long id,Long owner,BigDecimal value){amount(value);var a=owned(locked(id),owner);a.balance=a.balance.add(value);ledger.save(new LedgerEntry(id,value,"DEPOSIT"));return a;}
 @Transactional public Account withdraw(Long id,Long owner,BigDecimal value){amount(value);var a=owned(locked(id),owner);debit(a,value);ledger.save(new LedgerEntry(id,value.negate(),"WITHDRAWAL"));return a;}
 private void debit(Account a,BigDecimal value){if(a.balance.compareTo(value)<0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient funds");a.balance=a.balance.subtract(value);}
 @Transactional public void transfer(Long from,Long to,Long owner,BigDecimal value){amount(value);if(from.equals(to))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Different accounts required");var first=locked(Math.min(from,to));var second=locked(Math.max(from,to));var source=owned(first.id.equals(from)?first:second,owner);var destination=first.id.equals(to)?first:second;debit(source,value);destination.balance=destination.balance.add(value);ledger.save(new LedgerEntry(from,value.negate(),"TRANSFER_OUT"));ledger.save(new LedgerEntry(to,value,"TRANSFER_IN"));}
}
