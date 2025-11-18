package com.example.bigfood.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {
  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
  ACCOUNT_NOT_FIND(1000, "No account yet !"),
  AUTHENTICATION_FAILED(1001, "Your password is incorrect !"),
  UNAUTHENTICATED(1004, "Bạn không có quyền truy cập"),
  PERMISSION_EXITED(1009, "Permission exited"),
  Role_EXITED(1009, "Role exited"),
  Role_NOT_FIND(1009, "Role exited"),
  RESTAURANT_CATEGORY_ALREADY_EXISTS(1005, "Restaurant category already exists"),
  RESTAURANT_CATEGORY_NOT_EXISTS(1005, "Restaurant category not exists");


  private int code;
  private String message;
}