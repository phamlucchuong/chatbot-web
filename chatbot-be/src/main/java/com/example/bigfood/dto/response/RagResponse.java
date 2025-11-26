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
public class RagResponse {
    boolean success;
    String disease_id;
    String disease_name;
    String response;
    List<SimilarDiseaseResponse> similar_diseases;
    String error;
}
