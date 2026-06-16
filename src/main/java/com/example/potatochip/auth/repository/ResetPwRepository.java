package com.example.potatochip.auth.repository;

import com.example.potatochip.auth.entity.ResetPw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetPwRepository extends JpaRepository<ResetPw, Long> {
    Optional<ResetPw> findByToken(String token);
    void deleteByUser_Id(Long userId);
}
