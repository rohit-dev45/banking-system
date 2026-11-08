package dev.rohit.app;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/accounts")
public class BankingController {
 record Money(BigDecimal amount){} record Transfer(Long destinationId,BigDecimal amount){}
 private final Accounts accounts;private final Ledger ledger;private final BankingService service;
 BankingController(Accounts a,Ledger l,BankingService s){accounts=a;ledger=l;service=s;}
 private Long owner(Jwt jwt){return Long.valueOf(jwt.getSubject());}
 private Account owned(Long id,Jwt jwt){return service.owned(accounts.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)),owner(jwt));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Account create(@AuthenticationPrincipal Jwt jwt){return accounts.save(new Account(owner(jwt)));}
 @GetMapping public List<Account> list(@AuthenticationPrincipal Jwt jwt){return accounts.findByOwnerId(owner(jwt));}
 @GetMapping("/{id}") public Account get(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt){return owned(id,jwt);}
 @PostMapping("/{id}/deposit") public Account deposit(@PathVariable Long id,@RequestBody Money m,@AuthenticationPrincipal Jwt jwt){return service.deposit(id,owner(jwt),m.amount());}
 @PostMapping("/{id}/withdraw") public Account withdraw(@PathVariable Long id,@RequestBody Money m,@AuthenticationPrincipal Jwt jwt){return service.withdraw(id,owner(jwt),m.amount());}
 @PostMapping("/{id}/transfer") @ResponseStatus(HttpStatus.NO_CONTENT) public void transfer(@PathVariable Long id,@RequestBody Transfer t,@AuthenticationPrincipal Jwt jwt){if(t.destinationId()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);service.transfer(id,t.destinationId(),owner(jwt),t.amount());}
 @GetMapping("/{id}/transactions") public Page<LedgerEntry> history(@PathVariable Long id,@RequestParam(defaultValue="0") int page,@AuthenticationPrincipal Jwt jwt){owned(id,jwt);return ledger.findByAccountIdOrderByIdDesc(id,PageRequest.of(Math.max(page,0),20));}
}
