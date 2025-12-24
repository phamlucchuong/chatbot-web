package com.example.chatbot.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class GoongResponse {
    // Ví dụ về cấu trúc JSON từ Goong Geocoding
    public static class GoongGeometry {
        private GoongLocation location;

        public GoongLocation getLocation() {
            return location;
        }

        public void setLocation(GoongLocation location) {
            this.location = location;
        }
    }

    public static class GoongLocation {
        private double lat; // Latitude
        private double lng; // Longitude

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }

    public static class GoongResult {
        private GoongGeometry geometry;
        private String formatted_address;

        public GoongGeometry getGeometry() {
            return geometry;
        }

        public void setGeometry(GoongGeometry geometry) {
            this.geometry = geometry;
        }

        public String getFormatted_address() {
            return formatted_address;
        }

        public void setFormatted_address(String formatted_address) {
            this.formatted_address = formatted_address;
        }
    }

    public static class GeocodingResponse {
        private List<GoongResult> results;
        private String status;

        public List<GoongResult> getResults() {
            return results;
        }

        public void setResults(List<GoongResult> results) {
            this.results = results;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // Đặt cùng chỗ với các class GoongLocation, GeocodingResponse, v.v.

    public static class GoongLeg {
        private GoongDistance distance;
        // Có thể thêm duration nếu cần tính thời gian
        // private GoongDuration duration;

        public GoongDistance getDistance() {
            return distance;
        }

        public void setDistance(GoongDistance distance) {
            this.distance = distance;
        }
    }

    public static class GoongDistance {
        private String text; // Ví dụ: "5.2 km"
        private int value; // Ví dụ: 5200 (mét)

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class DirectionRoute {
        private List<GoongLeg> legs;

        public List<GoongLeg> getLegs() {
            return legs;
        }

        public void setLegs(List<GoongLeg> legs) {
            this.legs = legs;
        }
    }

    public static class DirectionResponse {
        private List<DirectionRoute> routes;

        public List<DirectionRoute> getRoutes() {
            return routes;
        }

        public void setRoutes(List<DirectionRoute> routes) {
            this.routes = routes;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class HospitalResult {
        private String name;
        private String vicinity; // Địa chỉ
        private GoongGeometry geometry;
        // Getters and Setters
        public GoongGeometry getGeometry() {
            return geometry;
        }
        public void setGeometry(GoongGeometry geometry) {
            this.geometry = geometry;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getVicinity() {
            return vicinity;
        }
        public void setVicinity(String vicinity) {
            this.vicinity = vicinity;
        }
    }

    public static class HospitalResponse {
        private String errors;
        private List<HospitalResult> features;
        // Getters and Setters
        public List<HospitalResult> getFeatures() {
            return features;
        }
        public void setFeatures(List<HospitalResult> features) {
            this.features = features;
        }
        public String getErrors() {
            return errors;
        }
        public void setErrors(String errors) {
            this.errors = errors;
        }
    }
}
