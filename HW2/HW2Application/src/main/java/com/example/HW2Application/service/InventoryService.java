package com.example.HW2Application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;

@Service
public class InventoryService {

    private final String INVENTORY_API_URL = "http://localhost:3000";

    private final RestTemplate restTemplate;

    public InventoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAllProducts() {
        return restTemplate.getForObject(INVENTORY_API_URL + "/products", String.class);
    }

    public String addProduct(Map<String, Object> productData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(productData, headers);
        return restTemplate.postForObject(INVENTORY_API_URL + "/products", request, String.class);
    }


    public void deleteProduct(Long id) {
        restTemplate.delete(INVENTORY_API_URL + "/products/" + id);
    }

    public String checkStock(Long id) {
        return restTemplate.getForObject(INVENTORY_API_URL + "/inventory/check/" + id, String.class);
    }

    public String getLowStockNotifications() {
        return restTemplate.getForObject(INVENTORY_API_URL + "/inventory/notify", String.class);
    }
}
