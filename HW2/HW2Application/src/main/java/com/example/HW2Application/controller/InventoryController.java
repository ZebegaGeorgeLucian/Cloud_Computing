package com.example.HW2Application.controller;

import com.example.HW2Application.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/inventory")
@CrossOrigin(origins = "http://localhost:5173")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products")
    public ResponseEntity<String> getAllProducts(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Missing or invalid token");
        }
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }


    @PostMapping("/products")
    public String addProduct(@RequestBody Map<String, Object> productData) {
        return inventoryService.addProduct(productData);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable Long id) {
        inventoryService.deleteProduct(id);
    }

    @GetMapping("/inventory/check/{id}")
    public String checkStock(@PathVariable Long id) {
        return inventoryService.checkStock(id);
    }

    @GetMapping("/inventory/notify")
    public String getLowStockNotifications() {
        return inventoryService.getLowStockNotifications();
    }
}
