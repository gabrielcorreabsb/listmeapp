package com.listme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    //método para debug
    @GetMapping("/auth/login")
    public String loginError() {
        return "redirect:/login?error=true";
    }
}