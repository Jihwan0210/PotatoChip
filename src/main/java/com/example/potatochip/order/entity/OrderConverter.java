package com.example.potatochip.order.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OrderConverter implements AttributeConverter<OrderStatus, String> {
    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        if (attribute == null) {
            return null;
        }
        // Java enum(대문자) → DB 컬럼(소문자)
        return attribute.name().toLowerCase();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // DB 컬럼(소문자) → Java enum(대문자)
        return OrderStatus.valueOf(dbData.toUpperCase());
    }
}

