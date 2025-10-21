package com.example.demo.controller.web;

import com.example.demo.model.request.CustomerRegisterRequest;
import com.example.demo.model.request.OtpVerificationRequest;
import com.example.demo.model.response.ResponseDTO;
import com.example.demo.service.CustomerService;
import com.example.demo.service.OtpService;
import jakarta.servlet.http.HttpSession;
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

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private static final String PENDING_REGISTRATION_SESSION_KEY = "PENDING_REGISTER_CUSTOMER";

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OtpService otpService;

    @GetMapping
    public String showRegisterForm() {
        return "web/register";
    }

    @GetMapping("/otp")
    public String showOtpForm(HttpSession session) {
        CustomerRegisterRequest pending = (CustomerRegisterRequest) session.getAttribute(PENDING_REGISTRATION_SESSION_KEY);
        if (pending == null) {
            return "redirect:/register";
        }
        return "web/otp";
    }

    @GetMapping("/pending")
    @ResponseBody
    public ResponseEntity<ResponseDTO> getPendingRegistration(HttpSession session) {
        ResponseDTO response = new ResponseDTO();
        CustomerRegisterRequest pending = (CustomerRegisterRequest) session.getAttribute(PENDING_REGISTRATION_SESSION_KEY);
        if (pending == null) {
            response.setMessage("Không tìm thấy thông tin đăng ký đang chờ xử lý.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("fullname", pending.getFullname());
        data.put("phone", pending.getPhone());
        data.put("email", pending.getEmail());
        data.put("username", pending.getUsername());
        data.put("companyname", pending.getCompanyname());
        data.put("demand", pending.getDemand());
        data.put("status", pending.getStatus());
        response.setData(data);
        response.setMessage("Thông tin đăng ký đang chờ xác thực.");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ResponseDTO> startRegistration(@RequestBody CustomerRegisterRequest request, HttpSession session) {
        ResponseDTO response = new ResponseDTO();

        if (!StringUtils.hasText(request.getFullname()) || !StringUtils.hasText(request.getPhone())
                || !StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getPassword())) {
            response.setMessage("Vui lòng điền đầy đủ các trường bắt buộc trước khi tiếp tục.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!StringUtils.hasText(request.getStatus())) {
            request.setStatus("Chờ kích hoạt");
        }

        request.setOtp(null);
        request.setOtpTarget(null);
        session.setAttribute(PENDING_REGISTRATION_SESSION_KEY, request);

        Map<String, Object> data = new HashMap<>();
        data.put("redirectUrl", "/register/otp");
        response.setData(data);
        response.setMessage("Thông tin đăng ký đã được ghi nhận. Vui lòng xác thực OTP để hoàn tất.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    @ResponseBody
    public ResponseEntity<ResponseDTO> verifyRegistration(@RequestBody OtpVerificationRequest otpRequest, HttpSession session) {
        ResponseDTO response = new ResponseDTO();
        CustomerRegisterRequest pending = (CustomerRegisterRequest) session.getAttribute(PENDING_REGISTRATION_SESSION_KEY);
        if (pending == null) {
            response.setMessage("Không tìm thấy thông tin đăng ký đang chờ xác thực.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!StringUtils.hasText(otpRequest.getOtp())) {
            response.setMessage("Vui lòng nhập mã OTP trước khi xác thực.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String channel = StringUtils.hasText(otpRequest.getChannel()) ? otpRequest.getChannel().toLowerCase() : null;
        String otpKey = null;
        if ("sms".equals(channel)) {
            otpKey = pending.getPhone();
            channel = "sms";
        } else {
            otpKey = pending.getEmail();
            channel = "email";
        }

        if (!StringUtils.hasText(otpKey)) {
            response.setMessage("Không thể xác định thông tin nhận OTP. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!otpRequest.getOtp().matches("\\d{6}")) {
            response.setMessage("Mã OTP phải gồm 6 chữ số.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean valid = otpService.validateOtp(otpKey, otpRequest.getOtp());
        if (!valid) {
            response.setMessage("Mã OTP không đúng hoặc đã hết hạn. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        pending.setOtpTarget(channel);
        ResponseDTO serviceResponse = customerService.save(pending);
        if (serviceResponse == null) {
            response.setMessage("Không thể lưu thông tin đăng ký. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        String serviceMessage = serviceResponse.getMessage();
        boolean success = !StringUtils.hasText(serviceMessage) || !serviceMessage.trim().startsWith("❌");
        if (success) {
            session.removeAttribute(PENDING_REGISTRATION_SESSION_KEY);
            if (!StringUtils.hasText(serviceMessage)) {
                serviceResponse.setMessage("Đăng ký thành công");
            }
            return ResponseEntity.ok(serviceResponse);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceResponse);
    }
}
