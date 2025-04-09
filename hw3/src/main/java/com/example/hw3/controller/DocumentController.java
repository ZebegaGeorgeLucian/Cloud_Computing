package com.example.hw3.controller;

import com.example.hw3.service.GcsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final GcsService gcsService;

    public DocumentController(GcsService gcsService) {
        this.gcsService = gcsService;
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        System.out.println("Incoming Request Content-Type: " + request.getContentType());
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty or missing");
        }
        try {
            String gcsUri = gcsService.uploadAndProcessFile(file); // Use the processing method
            return ResponseEntity.ok("File uploaded and processed successfully. GCS URI: " + gcsUri);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while uploading and processing file: " + e.getMessage());
        }
    }
}