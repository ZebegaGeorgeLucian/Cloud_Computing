package com.example.HW2Application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<String> testAuth(Principal principal) {
        return ResponseEntity.ok("Hello, authenticated user: " + principal.getName());
    }
}