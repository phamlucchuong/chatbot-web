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
  DISEASE_NOT_FOUND(1010, "Disease not found"),
  MAPBOX_TOKEN_MISSING(2000, "Mapbox access token is not configured"),
  MAPBOX_UNAUTHORIZED(2001, "Không thể xác thực với Mapbox, vui lòng kiểm tra token"),
  MAPBOX_BAD_REQUEST(2002, "Mapbox từ chối truy vấn, kiểm tra tham số đầu vào"),
  MAPBOX_DOWNSTREAM_ERROR(2003, "Mapbox đang gặp sự cố, thử lại sau");


  private int code;
  private String message;
}