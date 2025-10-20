package com.example.demo.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRegisterRequest extends CustomerCreateRequest {
    private String otp;
    private String otpTarget; // email or sms
}
