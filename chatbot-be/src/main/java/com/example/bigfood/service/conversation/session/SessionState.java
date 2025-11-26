package com.example.bigfood.service.conversation.session;

import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class SessionState {
    private String diagnosedDiseaseId;
    private String diagnosedDiseaseName;
    private Set<String> askedTopics = new HashSet<>();
    private boolean isDiagnosed = false;
    private int diagnosisAttempts = 0;
    private boolean greeted = false;
    private boolean sessionClosed = false;

    public void resetFullSession() {
        this.diagnosedDiseaseId = null;
        this.diagnosedDiseaseName = null;
        this.askedTopics.clear();
        this.isDiagnosed = false;
        this.diagnosisAttempts = 0;
        this.greeted = false;
        this.sessionClosed = false;
    }
}