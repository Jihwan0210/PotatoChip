package com.example.potatochip.product.repository;

import com.example.potatochip.product.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository <Board,Long> {
}
