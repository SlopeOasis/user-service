package com.slopeoasis.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/whoami")
public class WhoAmICont {
    @GetMapping
    public String whoami() {
        return System.getenv("HOSTNAME");
    }
}
