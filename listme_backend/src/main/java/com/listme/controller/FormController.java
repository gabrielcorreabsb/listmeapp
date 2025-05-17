package com.listme.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FormController {

    @GetMapping("/form")
    public String showForm(Model model, Authentication authentication) {
        // Pega o usuário autenticado
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        return "form"; // Isso vai procurar por form.html
    }
}