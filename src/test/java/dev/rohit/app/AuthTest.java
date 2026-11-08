package dev.rohit.app;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.Map;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthTest {
 @Autowired TestRestTemplate http;
 @Test void registrationLoginAndProtection(){var username="user_"+System.nanoTime();var credentials=Map.of("username",username,"password","safe-password-123");assertEquals(201,http.postForEntity("/api/auth/register",credentials,String.class).getStatusCode().value());var login=http.postForEntity("/api/auth/login",credentials,Map.class);assertEquals(200,login.getStatusCode().value());assertNotNull(login.getBody().get("token"));assertEquals(401,http.postForEntity("/api/auth/login",Map.of("username",username,"password","wrong-pass"),String.class).getStatusCode().value());assertEquals(401,http.getForEntity("/api/unknown",String.class).getStatusCode().value());var headers=new HttpHeaders();headers.setBearerAuth((String)login.getBody().get("token"));var path="/api/"+"accounts";assertEquals(200,http.exchange(path,HttpMethod.GET,new HttpEntity<>(headers),String.class).getStatusCode().value());}
}
