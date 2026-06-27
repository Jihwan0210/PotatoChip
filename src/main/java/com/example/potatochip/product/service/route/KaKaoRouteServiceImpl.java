package com.example.potatochip.product.service.route;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KaKaoRouteServiceImpl implements KaKaoRouteService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    @Override
    public String buildKakaoRouteUrl(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        if (user.getAddress() == null || user.getAddress().isBlank()) {
            throw new IllegalStateException("내 주소가 등록되어 있지 않아요. 마이페이지에서 먼저 등록해주세요.");
        }

        Product product = productRepository.findById(productId).orElseThrow();
        if (product.getLatitude() == null || product.getLongitude() == null) {
            throw new IllegalStateException("판매처 위치 정보가 없어요.");
        }

        double[] myCoords = geocode(user.getAddress());

        String fromName = URLEncoder.encode("내위치", StandardCharsets.UTF_8);
        String toName = URLEncoder.encode(product.getName(), StandardCharsets.UTF_8);

        return "https://map.kakao.com/link/from/" + fromName + "," + myCoords[0] + "," + myCoords[1]
                + "/to/" + toName + "," + product.getLatitude() + "," + product.getLongitude();
    }

    private double[] geocode(String address) {
        java.net.URI uri = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", address)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);

        ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        List<Map<String, Object>> documents = (List<Map<String, Object>>) response.getBody().get("documents");
        if (documents == null || documents.isEmpty()) {
            throw new IllegalStateException("주소를 좌표로 변환하지 못했어요.");
        }

        Map<String, Object> first = documents.get(0);
        double lat = Double.parseDouble((String) first.get("y"));
        double lng = Double.parseDouble((String) first.get("x"));
        return new double[]{lat, lng};
    }
}