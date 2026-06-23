package com.example.potatochip.traking.service;

import com.example.potatochip.traking.dto.DeliveryTrackingDTO;

public interface DeliveryTrackingService {
    DeliveryTrackingDTO getTracking(Long orderId);
    void updateLocation(Long orderId, Double latitude, Double longitude);
}
