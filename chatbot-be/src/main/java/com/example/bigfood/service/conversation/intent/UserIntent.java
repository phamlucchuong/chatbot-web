package com.example.bigfood.service.conversation.intent;

public enum UserIntent {
    GREETING,            // Lời chào
    VAGUE_COMPLAINT,     // Than phiền chung chung
    SYMPTOM_DESCRIPTION, // Mô tả triệu chứng cụ thể
    ASK_DISEASE_INFO,    // Hỏi thông tin bệnh
    DECLARE_DISEASE,     // Tự khai báo bệnh
    FAREWELL,            // Tạm biệt
    THANKS,              // Cảm ơn
    OFF_TOPIC,           // Không liên quan
    ASK_MEDICAL_CONTEXT, // Hỏi về ngữ cảnh y tế
    OTHER                // Khác
}