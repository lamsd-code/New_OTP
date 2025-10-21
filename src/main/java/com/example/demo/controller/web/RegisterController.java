package com.example.demo.controller.web;

import com.example.demo.model.request.CustomerRegisterRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/register")
public class RegisterController {


    @Autowired
    private CustomerService customerService;

    @Autowired
    private OtpService otpService;

    @GetMapping
    public String showRegisterForm() {
        return "web/register";
    }


        if (!valid) {
            response.setMessage("Mã OTP không đúng hoặc đã hết hạn. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }


    }
}
