package com.example.potatochip.product.entity.ranking;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "product_rankings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductRankings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType periodType;

    @Column(nullable = false)
    private LocalDate periodDate;

    @Column(nullable = false)
    private int salesCount = 0;

    @Column(name = "ranking", nullable = false)
    private int rank;



}
