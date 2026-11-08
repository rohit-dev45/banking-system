package dev.rohit.app;
import jakarta.persistence.*;
@Entity @Table(name="members")
public class Member {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true,length=80) public String username;
 @Column(nullable=false) public String passwordHash;
 protected Member() {} Member(String username,String hash){this.username=username;this.passwordHash=hash;}
}
