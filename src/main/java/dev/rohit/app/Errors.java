package dev.rohit.app;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
@RestControllerAdvice
class Errors {
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> conflict(){return ResponseEntity.status(409).body(Map.of("error","Record conflicts with existing data"));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> invalid(){return ResponseEntity.badRequest().body(Map.of("error","Invalid request fields"));}
}
