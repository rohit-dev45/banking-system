package dev.rohit.app;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
@SpringBootTest class BankingTest {
 @Autowired Accounts accounts; @Autowired Ledger ledger; @Autowired BankingService service;
 @BeforeEach void reset(){ledger.deleteAll();accounts.deleteAll();}
 @Test void transferConservesFundsAndWritesBothEntries(){var a=accounts.save(new Account(1L));var b=accounts.save(new Account(2L));service.deposit(a.id,1L,new BigDecimal("100.00"));service.transfer(a.id,b.id,1L,new BigDecimal("35.00"));assertEquals(0,accounts.findById(a.id).orElseThrow().balance.compareTo(new BigDecimal("65")));assertEquals(0,accounts.findById(b.id).orElseThrow().balance.compareTo(new BigDecimal("35")));assertEquals(3,ledger.count());}
 @Test void insufficientFundsLeaveBothAccountsAndLedgerUnchanged(){var a=accounts.save(new Account(1L));var b=accounts.save(new Account(2L));assertThrows(ResponseStatusException.class,()->service.transfer(a.id,b.id,1L,BigDecimal.TEN));assertEquals(0,ledger.count());assertEquals(0,accounts.findById(b.id).orElseThrow().balance.signum());}
 @Test void anotherUserCannotDebitOrCreditAccount(){var a=accounts.save(new Account(1L));assertThrows(ResponseStatusException.class,()->service.deposit(a.id,2L,BigDecimal.TEN));assertEquals(0,ledger.count());}
 @Test void negativeAndFractionalPaiseAreRejected(){assertThrows(ResponseStatusException.class,()->BankingService.amount(new BigDecimal("-1")));assertThrows(ResponseStatusException.class,()->BankingService.amount(new BigDecimal("1.001")));}
}
