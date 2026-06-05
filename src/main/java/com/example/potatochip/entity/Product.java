package com.example.potatochip.entity;

import com.example.potatochip.dto.response.ProductResponseDTO;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.web.bind.annotation.PostMapping;


@Getter
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private int stock;

    public Long getId() {return id; }
    public String getName() {return name; }
    public int getPrice() {return price; }
    public int getStock() {return stock; }

        public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
