package com.example.chatbot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.chatbot.dto.response.GoongResponse.DirectionResponse;
import com.example.chatbot.dto.response.GoongResponse.DirectionRoute;
import com.example.chatbot.dto.response.GoongResponse.GeocodingResponse;
import com.example.chatbot.dto.response.GoongResponse.GoongLocation;
import com.example.chatbot.dto.response.HospitalResponse;
import com.example.chatbot.dto.response.OverpassResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapService {

        @Value("${goong.api.key}")
        private String GOONG_API_KEY;

        @Value("${goong.api.base-url}")
        private String GOONG_BASE_URL;

        private final WebClient overpassWebClient;

        private final WebClient goongWebClient;

        public GoongLocation getGeocoding(String address) {
                String geocodingPath = "geocode";

                String url = UriComponentsBuilder.fromHttpUrl(GOONG_BASE_URL)
                                .path(geocodingPath)
                                .queryParam("address", address)
                                .queryParam("api_key", GOONG_API_KEY)
                                .build()
                                .toUriString();

                GeocodingResponse response = goongWebClient.get()
                                .uri(url)
                                .retrieve()
                                .bodyToMono(GeocodingResponse.class)
                                .block();

                // 3. Xử lý kết quả
                if (response != null && response.getStatus().equals("OK") && !response.getResults().isEmpty()) {
                        return response.getResults().get(0).getGeometry().getLocation();
                }

                // Xử lý khi không tìm thấy hoặc lỗi
                throw new RuntimeException("Không tìm thấy tọa độ cho địa chỉ: " + address);
        }

        public String getReverseGeocoding(GoongLocation geocoding) {
                String geocodingPath = "geocode";

                String url = UriComponentsBuilder.fromHttpUrl(GOONG_BASE_URL)
                                .path(geocodingPath)
                                .queryParam("latlng", geocoding.getLat() + "," + geocoding.getLng())
                                .queryParam("api_key", GOONG_API_KEY)
                                .build()
                                .toUriString();

                GeocodingResponse response = goongWebClient.get()
                                .uri(url)
                                .retrieve()
                                .bodyToMono(GeocodingResponse.class)
                                .block();

                // 3. Xử lý kết quả
                if (response != null && response.getStatus().equals("OK") &&
                                !response.getResults().isEmpty()) {
                        return response.getResults().get(0).getFormatted_address();
                }

                // Xử lý khi không tìm thấy hoặc lỗi
                throw new RuntimeException("Không tìm thấy tọa độ cho địa chỉ: " +
                                geocoding.getLat() + "," + geocoding.getLng());
        }

        /**
         * Lấy khoảng cách di chuyển (tính bằng kilomet) giữa hai tọa độ.
         * * @param originLat Tọa độ Latitude điểm đi
         * 
         * @param originLng      Tọa độ Longitude điểm đi
         * @param destinationLat Tọa độ Latitude điểm đến
         * @param destinationLng Tọa độ Longitude điểm đến
         * @return Khoảng cách tính bằng kilomet.
         */
        public double getDrivingDistance(double originLat, double originLng, double destinationLat,
                        double destinationLng) {
                String directionPath = "Direction";

                String origin = originLat + "," + originLng;
                String destination = destinationLat + "," + destinationLng;

                String url = UriComponentsBuilder.fromHttpUrl(GOONG_BASE_URL)
                                .path(directionPath)
                                .queryParam("origin", origin)
                                .queryParam("destination", destination)
                                .queryParam("vehicle", "bike")
                                .queryParam("api_key", GOONG_API_KEY)
                                .build()
                                .toUriString();

                DirectionResponse response = goongWebClient.get()
                                .uri(url)
                                .retrieve()
                                .bodyToMono(DirectionResponse.class)
                                .block();

                // 🎯 LOGIC ĐÃ SỬA: Bỏ kiểm tra status, chỉ kiểm tra sự tồn tại của routes
                if (response != null && response.getRoutes() != null && !response.getRoutes().isEmpty()) {

                        // Kiểm tra an toàn cho legs trước khi truy cập
                        DirectionRoute route = response.getRoutes().get(0);

                        // Đảm bảo legs tồn tại và không rỗng
                        if (route.getLegs() != null && !route.getLegs().isEmpty()) {
                                // Lấy khoảng cách (value) tính bằng mét
                                int distanceInMeters = route.getLegs().get(0).getDistance().getValue();

                                // 🎯 Lưu ý: Hàm của bạn trả về int, nên nếu bạn chia cho 1000 để lấy KM,
                                // kết quả sẽ bị làm tròn xuống. Nếu cần chính xác, hãy trả về double.
                                System.out.println("Distance in meters: " + distanceInMeters / 1000.0);
                                return distanceInMeters / 1000.0;
                        }
                }

                // Xử lý khi không tìm thấy đường đi hoặc lỗi API
                throw new RuntimeException(
                                "Không tìm thấy tuyến đường hợp lệ hoặc API trả về phản hồi rỗng giữa: " + origin
                                                + " và " + destination);
        }

        public List<HospitalResponse> findNearbyHospitals(double lat, double lng, int radius) {

                // Xây dựng URL với tham số
                String query = String.format(java.util.Locale.US, """
                                [out:json][timeout:15];
                                (
                                  way["amenity"="hospital"](around:%d,%f,%f);
                                );
                                out center tags;
                                """, radius, lat, lng);
                OverpassResponse response = overpassWebClient.post()
                                .uri("/api/interpreter")
                                .header("Content-Type", "text/plain")
                                .bodyValue(query)
                                .retrieve()
                                .bodyToMono(OverpassResponse.class)
                                .block();

                return response.getElements().stream().map(element -> {
                        HospitalResponse hospital = new HospitalResponse();
                        hospital.setLat(element.getCenter().getLat());
                        hospital.setLng(element.getCenter().getLon());
                        hospital.setName(element.getTags().get("name"));
                        return hospital;
                }).toList();

        }
}
