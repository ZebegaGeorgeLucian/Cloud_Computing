package com.example.HW2Application.service;

import com.example.HW2Application.dto.auth.LoginRequest;
import com.example.HW2Application.dto.auth.RegistrationRequest;
import com.example.HW2Application.entity.User;
import com.example.HW2Application.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // Use the Firebase Web API key from application.properties
    @Value("${firebase.apiKey}")
    private String firebaseApiKey;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public String registerUser(RegistrationRequest request) throws FirebaseAuthException {
        try {
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword());
            UserRecord firebaseUser = FirebaseAuth.getInstance().createUser(createRequest);
            System.out.println("Successfully created new user in Firebase with UID: " + firebaseUser.getUid());

            User user = new User();
            user.setFirebaseUid(firebaseUser.getUid());
            user.setEmail(request.getEmail());
            userRepository.save(user);
            System.out.println("User details saved to PostgreSQL for UID: " + firebaseUser.getUid());

            return firebaseUser.getUid();
        } catch (FirebaseAuthException e) {
            System.err.println("Error creating user in Firebase: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Authenticate the user and retrieve an ID token using Firebase's REST API.
     * The API key is injected from the application.properties file.
     */
    public String loginUser(LoginRequest request) {
        if (firebaseApiKey == null || firebaseApiKey.trim().isEmpty()) {
            throw new RuntimeException("Firebase API key is not configured. Please set firebase.apiKey in application.properties.");
        }

        // Build the Firebase REST API URL for login
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;

        // Create the login request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", request.getEmail());
        requestBody.put("password", request.getPassword());
        requestBody.put("returnSecureToken", true);

        // Send the POST request using RestTemplate
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

        // Check the response and extract the ID token
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("idToken")) {
                return (String) responseBody.get("idToken");
            } else {
                throw new RuntimeException("ID token not found in Firebase response. Response: " + responseBody);
            }
        } else {
            throw new RuntimeException("Firebase login failed with status: " + response.getStatusCode()
                    + ". Response body: " + response.getBody());
        }
    }
}
