package dev.rohit.app;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface Members extends JpaRepository<Member,Long> { Optional<Member> findByUsername(String username); }
