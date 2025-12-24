package com.example.chatbot.exception;

import com.example.chatbot.enums.ErrorCode;


import lombok.Getter;


@Getter

public class AppException extends RuntimeException {

    private ErrorCode errorCode;
    
    public AppException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
}
