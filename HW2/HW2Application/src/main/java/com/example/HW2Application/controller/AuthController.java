package com.example.HW2Application.controller;
import com.example.HW2Application.dto.auth.LoginRequest;
import com.example.HW2Application.dto.auth.RegistrationRequest;
import com.example.HW2Application.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest registrationRequest) {
        try {
            String uid = authService.registerUser(registrationRequest);
            return new ResponseEntity<>("User registered successfully with UID: " + uid, HttpStatus.CREATED);
        } catch (FirebaseAuthException e) {
            return new ResponseEntity<>("Error registering user: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.loginUser(loginRequest);
        return ResponseEntity.ok("User logged in successfully. Token: " + token);
    }


}