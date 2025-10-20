package com.example.demo.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDTO extends AbstractDTO{
    private String fullname;
    private String phone;
    private String email;
    private String username;
    private String password;
    private String demand;
    private String status;
}
