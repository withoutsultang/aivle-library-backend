package com.aivle8.backend.repository;

import com.aivle8.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNaverId(String naverId); // 이제 정상 동작
    Optional<User> findByEmail(String email);
}
