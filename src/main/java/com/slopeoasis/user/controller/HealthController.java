package com.slopeoasis.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check endpoint for debugging connectivity.
 */
@RestController
@RequestMapping("/healthuser")
public class HealthController {
    
    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User-service backend is running!");
    }
}
