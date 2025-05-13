package com.example.diplom.controllers;

import com.example.diplom.entity.User;
import com.example.diplom.repository.UserRepository;
import com.example.diplom.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {


    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        model.addAttribute("user", new User()); // модель для формы
        return "registration"; // HTML шаблон регистрации
    }

    @PostMapping("/registration")
    public String register(@ModelAttribute User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // шифруем пароль
        userRepository.save(user);
        return "redirect:/login";
    }


    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {

                return "redirect:/card";

        }

        return "login"; // если не авторизован — показать страницу входа
    }

    @GetMapping("/unauthenticated")
    public String unauthenticated() {
        return "unauthenticated"; // возвращает шаблон unauthenticated.html
    }

    @GetMapping("/auth/status")
    public String getAuthStatus(Authentication authentication) {
        return (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken))
                ? "authenticated" : "unauthenticated";
    }
}