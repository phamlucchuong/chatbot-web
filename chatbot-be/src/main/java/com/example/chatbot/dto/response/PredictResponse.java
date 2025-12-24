
package com.example.chatbot.dto.response;

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
public class PredictResponse {
    String disease_id;
    String disease_name;
    float confidence;
    List<String> matched_symptoms;
    List<String> unmatched_symptoms;
    List<DiseaseResponse> top_predictions;
}
