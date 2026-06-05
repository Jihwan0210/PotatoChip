package com.example.potatochip.controller;


import com.example.potatochip.dto.response.InventoryResponseDTO;
import com.example.potatochip.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "재고 API", description = "상품 재고 조회")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(
            summary = "재고 조회",
            description = "전체 상품의 재고를 조회합니다."
    )
    @GetMapping("/inventory")
    public List<InventoryResponseDTO> getInventory() {
        return inventoryService.getInventory();
    }
}