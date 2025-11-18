package com.example.bigfood.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {
  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
  USER_NOT_FOUND(1000, "No account yet !"),
  AUTHENTICATION_FAILED(1001, "Your password is incorrect !"),
  UNAUTHENTICATED(1004, "Bạn không có quyền truy cập"),
  DISEASE_NOT_FOUND(1010, "Disease not found");


  private int code;
  private String message;
}