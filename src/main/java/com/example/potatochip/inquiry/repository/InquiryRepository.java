package com.example.potatochip.inquiry.repository;

import com.example.potatochip.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    List<Inquiry> findByStatusAndIsActiveTrueOrderByCreatedAtDesc(String status);

    List<Inquiry> findByIsActiveTrueOrderByCreatedAtDesc();

    Optional<Inquiry> findByIdAndIsActiveTrue(Long id);
}