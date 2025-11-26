package com.example.bigfood.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các trường thừa không cần thiết
public class MapboxResponse {
    private List<Feature> features;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private String id;
        
        @JsonProperty("text")
        private String name; // Tên địa điểm (VD: Bệnh viện Đa khoa)

        @JsonProperty("place_name")
        private String address; // Địa chỉ đầy đủ

        private Geometry geometry;
        
        @JsonProperty("properties")
        private Properties properties; // Thông tin bổ sung
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String category; // Loại địa điểm
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        private List<Double> coordinates; // [Longitude, Latitude] - Lưu ý thứ tự!
    }
}