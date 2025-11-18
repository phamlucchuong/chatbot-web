package com.example.bigfood.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.bigfood.dto.request.PredictDiseaseRequest;
import com.example.bigfood.dto.request.SearchRequest;
import com.example.bigfood.dto.response.PredictResponse;
import com.example.bigfood.dto.response.SymptomResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ModelApiService {

    final RestTemplate restTemplate;

    @Value("${model.sympton.api.url:http://127.0.0.1:8000/api/extract-symptoms}")
    String symptonApiUrl;

    @Value("${model.predict.api.url:http://127.0.0.1:8000/api/predict-disease}")
    String predictApiUrl;



    public SymptomResponse extractSymptom(SearchRequest userMessage) { // Đổi tên param cho rõ nghĩa
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SearchRequest> entity = new HttpEntity<>(userMessage, headers);

            ResponseEntity<SymptomResponse> response = restTemplate.exchange(
                    symptonApiUrl, // URL tới endpoint /extract-symton
                    HttpMethod.POST,
                    entity,
                    SymptomResponse.class // Lớp Java mong đợi nhận về
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Loi khi goi API trich xuat trieu chung (NER): {}", e.getMessage());
            // Ném lỗi rõ ràng hơn
            throw new RuntimeException("Loi khi goi API trich xuat trieu chung (NER)", e);
        }
    }




    public PredictResponse predictDisease(List<String> symptoms) {
        try {
            PredictDiseaseRequest request = PredictDiseaseRequest.builder()
                    .symptoms(symptoms)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<PredictDiseaseRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<PredictResponse> response = restTemplate.exchange(
                    predictApiUrl,
                    HttpMethod.POST,
                    entity,
                    PredictResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling predict API: {}", e.getMessage());
            throw new RuntimeException("Failed to predict disease", e);
        }
    }
}
