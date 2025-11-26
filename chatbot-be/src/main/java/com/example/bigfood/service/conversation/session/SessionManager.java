package com.example.bigfood.service.conversation.session;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SessionManager {
    
    private final Map<String, SessionState> conversationStates = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> symptomContexts = new ConcurrentHashMap<>();
    
    public SessionState getOrCreateState(String conversationId) {
        return conversationStates.computeIfAbsent(conversationId, k -> new SessionState());
    }
    
    public void updateState(String conversationId, SessionState state) {
        conversationStates.put(conversationId, state);
    }
    
    public void removeState(String conversationId) {
        conversationStates.remove(conversationId);
        symptomContexts.remove(conversationId);
        log.info("Removed session state for conversation: {}", conversationId);
    }
    
    public Set<String> getSymptomContext(String conversationId) {
        return symptomContexts.computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet());
    }
    
    public void addSymptoms(String conversationId, Set<String> newSymptoms) {
        Set<String> context = getSymptomContext(conversationId);
        context.addAll(newSymptoms);
    }
    
    public void clearSymptomContext(String conversationId) {
        symptomContexts.remove(conversationId);
    }
    
    public void resetSession(String conversationId) {
        SessionState state = getOrCreateState(conversationId);
        state.resetFullSession();
        clearSymptomContext(conversationId);
        updateState(conversationId, state);
        log.info("Reset session for conversation: {}", conversationId);
    }
}