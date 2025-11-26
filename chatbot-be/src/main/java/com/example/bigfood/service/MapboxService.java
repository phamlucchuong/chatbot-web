package com.example.bigfood.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.bigfood.dto.response.MapboxResponse;
import com.example.bigfood.enums.ErrorCode;
import com.example.bigfood.exception.AppException;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapboxService {

    private static final String DEFAULT_KEYWORD = "supermarket"; // tránh lỗi encode ký tự đặc biệt

    @Value("${mapbox.api.token}")
    private String mapboxToken;

    @Value("${mapbox.api.url}")
    private String mapboxUrl;

    private final RestTemplate restTemplate;

    public List<HospitalDTO> findNearbyHospitals(double lat, double lng) {
        if (!StringUtils.hasText(mapboxToken)) {
            log.error("Mapbox token is missing. Please configure mapbox.api.token");
            throw new AppException(ErrorCode.MAPBOX_TOKEN_MISSING);
        }

        URI uri = UriComponentsBuilder
                .fromUriString(mapboxUrl + "{keyword}.json")
                .queryParam("access_token", mapboxToken)
                .queryParam("proximity", lng + "," + lat)
                .queryParam("types", "poi")
                .queryParam("limit", 5)
                .queryParam("language", "vi")
                .buildAndExpand(DEFAULT_KEYWORD)
                .encode()
                .toUri();

        System.out.println("Mapbox URI: " + uri.toString());

        try {
            MapboxResponse response = restTemplate.getForObject(uri, MapboxResponse.class);
            return mapToHospitalDTOs(response);
        } catch (HttpClientErrorException.Unauthorized ex) {
            log.error("Mapbox unauthorized error: {}", ex.getResponseBodyAsString());
            throw new AppException(ErrorCode.MAPBOX_UNAUTHORIZED);
        } catch (HttpClientErrorException.BadRequest ex) {
            log.error("Mapbox bad request: {}", ex.getResponseBodyAsString());
            throw new AppException(ErrorCode.MAPBOX_BAD_REQUEST);
        } catch (HttpClientErrorException ex) {
            log.error("Mapbox error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AppException(ErrorCode.MAPBOX_DOWNSTREAM_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error while calling Mapbox", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private List<HospitalDTO> mapToHospitalDTOs(MapboxResponse response) {
        List<HospitalDTO> result = new ArrayList<>();
        if (response != null && response.getFeatures() != null) {
            for (MapboxResponse.Feature feature : response.getFeatures()) {
                try {
                    if (feature.getGeometry() == null ||
                            feature.getGeometry().getCoordinates() == null ||
                            feature.getGeometry().getCoordinates().size() < 2) {
                        log.warn("Skip feature without valid coordinates: {}", feature.getName());
                        continue;
                    }

                    HospitalDTO dto = new HospitalDTO();
                    dto.setName(feature.getName() != null ? feature.getName() : feature.getAddress());
                    dto.setAddress(feature.getAddress() != null ? feature.getAddress() : "");
                    dto.setLongitude(feature.getGeometry().getCoordinates().get(0));
                    dto.setLatitude(feature.getGeometry().getCoordinates().get(1));
                    result.add(dto);
                } catch (Exception e) {
                    log.error("Failed to parse feature {}", feature.getId(), e);
                }
            }
        }
        return result;
    }

    @Data
    public static class HospitalDTO {
        private String name;
        private String address;
        private double latitude;
        private double longitude;
    }
}