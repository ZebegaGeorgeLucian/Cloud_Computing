package com.example.HW2Application.dto.auth;

import lombok.Getter;
import lombok.Setter;


public class RegistrationRequest {
    private String email;
    private String password;

    public RegistrationRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}