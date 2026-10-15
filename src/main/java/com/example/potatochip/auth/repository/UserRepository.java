package com.example.potatochip.auth.repository;

import com.example.potatochip.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // 포인트 사용·적립을 한 번에 처리한다. 보유 포인트가 부족하면 0을 반환
    // native: points 컬럼의 columnDefinition 때문에 JPQL 산술식이 잘못된 CAST로 변환됨
    @Modifying
    @Query(value = "UPDATE users SET points = points - :used + :earned " +
                   "WHERE id = :id AND points >= :used", nativeQuery = true)
    int usePoints(@Param("id") Long id, @Param("used") int used, @Param("earned") int earned);
}
