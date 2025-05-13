package com.example.diplom.controllers;

import com.example.diplom.entity.Match;
import com.example.diplom.entity.User;
import com.example.diplom.repository.UserRepository;
import com.example.diplom.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/card")
public class SwipeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchService matchService;

    @GetMapping
    public String getProfile(Model model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        User nextUser = matchService.getNextUnswipedUser(currentUser);
        if (nextUser != null) {
            model.addAttribute("users", List.of(nextUser)); // ✅ Обернули в список
        } else {
            model.addAttribute("users", List.of()); // Пустой список, если всё просмотрено
        }
        return "card";
    }
    @PostMapping("/like/{id}")
    public String likeProfile(@PathVariable Long id, Principal principal) {
        User fromUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        User toUser = userRepository.findById(id).orElseThrow();
        matchService.swipe(fromUser, toUser, true);
        return "redirect:/card";
    }

    @PostMapping("/pass/{id}")
    public String passProfile(@PathVariable Long id, Principal principal) {
        User fromUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        User toUser = userRepository.findById(id).orElseThrow();
        matchService.swipe(fromUser, toUser, false);
        return "redirect:/card";
    }

    @GetMapping("/liked-me")
    public String getUsersWhoLikedMe(Model model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<User> likedMe = matchService.getUsersWhoLikedMe(currentUser);
        model.addAttribute("likedMe", likedMe);
        return "liked-me";
    }

    @GetMapping("/matches")
    public String getMatches(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<Match> matches = matchService.getMatches(user);
        model.addAttribute("matches", matches);
        return "matches";
    }
}
