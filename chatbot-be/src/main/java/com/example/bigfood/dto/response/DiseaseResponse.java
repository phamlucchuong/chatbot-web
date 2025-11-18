package com.example.bigfood.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiseaseResponse {
    String disease;
    float confidence;
    List<String> matched_symptoms;
    List<String> unmatched_symptoms;
    // String description;
    // Double confidence;
}
