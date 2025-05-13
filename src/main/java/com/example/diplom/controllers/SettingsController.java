package com.example.diplom.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class SettingsController {

    @GetMapping("/setting")
    public String showSettingsPage(Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("currentUsername", username);
        return "setting"; // Файл settings.html
    }
}
