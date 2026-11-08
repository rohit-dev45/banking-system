package dev.rohit.app;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 record Credentials(@NotBlank @Pattern(regexp="[a-zA-Z0-9_]{3,80}") String username,@NotBlank @Size(min=8,max=72) String password){}
 private final Members members; private final PasswordEncoder passwords; private final JwtEncoder encoder;
 AuthController(Members m,PasswordEncoder p,JwtEncoder e){members=m;passwords=p;encoder=e;}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
 public Map<String,Long> register(@Valid @RequestBody Credentials c){return Map.of("id",members.saveAndFlush(new Member(c.username(),passwords.encode(c.password()))).id);}
 @PostMapping("/login") public Map<String,String> login(@Valid @RequestBody Credentials c){var m=members.findByUsername(c.username()).filter(u->passwords.matches(c.password(),u.passwordHash)).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));var now=Instant.now();var claims=JwtClaimsSet.builder().issuer("rohit-api").subject(m.id.toString()).issuedAt(now).expiresAt(now.plusSeconds(3600)).build();return Map.of("token",encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue(),"tokenType","Bearer");}
}
