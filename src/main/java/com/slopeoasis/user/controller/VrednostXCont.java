package com.slopeoasis.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.slopeoasis.user.entity.VrednostX;
import com.slopeoasis.user.service.VrednostXServ;

@RestController
@RequestMapping("/value")
public class VrednostXCont {
    private final VrednostXServ vredXSe;
    public VrednostXCont(VrednostXServ vredXSe){
        this.vredXSe = vredXSe;
    }

    // GET /value
    @GetMapping
    public VrednostX getValue() {
        return vredXSe.getVredX();
    }

    // PUT /value?newValue=10
    @PutMapping
    public VrednostX updateValue(@RequestParam int newValue) {
        return vredXSe.updateVredX(newValue);
    }

    // POST /value/increment
    @PostMapping("/increment")
    public VrednostX incrementValue() {
        return vredXSe.incVredX();
    }
    
}
