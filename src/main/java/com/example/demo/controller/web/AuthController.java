package com.example.demo.controller.web;

import com.example.demo.service.EmailService;
import com.example.demo.service.OtpService;
import com.example.demo.service.SmsService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserService userService;

    // ==========================
    // LOGIN
    // ==========================
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // Delegate thực tế cho Spring Security
        return "IMPLEMENT_LOGIN_WITH_SPRING_SECURITY";
    }

    // ==========================
    // SEND OTP BY EMAIL
    // ==========================
    @PostMapping("/otp/send/email")
    public String sendOtpEmail(@RequestParam String email) {
        String otp = otpService.generateOtp(email);
        emailService.sendOtp(email, otp);
        return "OTP sent to your email. Please check your inbox. OTP = " + otp;
    }

    // ==========================
    // SEND OTP BY SMS
    // ==========================
    @PostMapping("/otp/send/sms")
    public String sendOtpSms(@RequestParam String phone) {
        String otp = otpService.generateOtp(phone);
        smsService.sendOtp(phone, otp);
        return "OTP sent to your phone. Please check your messages. OTP = " + otp;
    }

    // ==========================
    // VERIFY OTP
    // ==========================
    @PostMapping("/otp/verify")
    public String verifyOtp(@RequestParam String key, @RequestParam String otp) {
        boolean isValid = otpService.validateOtp(key, otp);
        return isValid ? "OTP Verified Successfully" : "Invalid or Expired OTP";
    }
}
