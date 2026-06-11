package com.example.potatochip.admin.service;


import com.example.potatochip.admin.dto.DashboardDTO;
import com.example.potatochip.board.repository.BoardRepository;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final ProductRepository productRepository;
    private final BoardRepository boardRepository;

    @Override
    public DashboardDTO getDashboard() {

        long totalProducts = productRepository.count();

        long totalBoards = boardRepository.count();

        long lowStockProducts =
                productRepository.countByStockQuantityLessThanEqual(10);

        long pickupProducts =
                productRepository.countByIsPickupAvailableTrue();

        return new DashboardDTO(
                totalProducts,
                totalBoards,
                lowStockProducts,
                pickupProducts

        );
    }
}
