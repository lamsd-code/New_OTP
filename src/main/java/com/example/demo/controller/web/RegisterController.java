package com.example.demo.controller.web;

import com.example.demo.model.request.CustomerRegisterRequest;
import com.example.demo.model.response.ResponseDTO;
import com.example.demo.service.CustomerService;
import com.example.demo.service.OtpService;
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

    @PostMapping
    @ResponseBody
    public ResponseEntity<ResponseDTO> register(@RequestBody CustomerRegisterRequest request) {
        ResponseDTO response = new ResponseDTO();

        String otpKey = resolveOtpKey(request);
        if (!StringUtils.hasText(otpKey)) {
            response.setMessage("Vui lòng chọn phương thức nhận OTP và nhập đầy đủ thông tin.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!StringUtils.hasText(request.getOtp())) {
            response.setMessage("Vui lòng nhập mã OTP để tiếp tục.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean valid = otpService.validateOtp(otpKey, request.getOtp());
        if (!valid) {
            response.setMessage("Mã OTP không đúng hoặc đã hết hạn. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        ResponseDTO serviceResponse = customerService.save(request);
        if (!StringUtils.hasText(serviceResponse.getMessage())) {
            serviceResponse.setMessage("Đăng ký thành công");
        }
        return ResponseEntity.ok(serviceResponse);
    }

    private String resolveOtpKey(CustomerRegisterRequest request) {
        if (StringUtils.hasText(request.getOtpTarget())) {
            if ("sms".equalsIgnoreCase(request.getOtpTarget())) {
                return request.getPhone();
            }
            return request.getEmail();
        }

        // Fallback: try email first, then phone
        if (StringUtils.hasText(request.getEmail())) {
            return request.getEmail();
        }
        if (StringUtils.hasText(request.getPhone())) {
            return request.getPhone();
        }
        return null;
    }
}
