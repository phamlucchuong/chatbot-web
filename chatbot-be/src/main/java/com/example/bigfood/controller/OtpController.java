// package com.example.bigfood.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;

// import com.example.bigfood.dto.response.ApiResponse;
// import com.example.bigfood.service.OtpService;

// @RestController
// @RequestMapping("/api/otp")
// public class OtpController {

//     @Autowired
//     private OtpService otpService;

//     @PostMapping("/send/email")
//     public ApiResponse<String> sendOtpEmail(@RequestParam String email) {
//         String otp = otpService.generateOtp(email);
//         otpService.sendOtp(email, otp);
//         return ApiResponse.<String>builder()
//                 .results("OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư!")
//                 .build();
//     }

//     @GetMapping("/verify")
//     public ApiResponse<Boolean> verifyOtp(@RequestParam String key, @RequestParam String otp) {
//         boolean verified = otpService.validateOtp(key, otp);
//         return ApiResponse.<Boolean>builder()
//                 .results(verified)
//                 .build();
//     }
// }
