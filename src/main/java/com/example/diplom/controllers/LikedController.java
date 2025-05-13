package com.example.diplom.controllers;

import com.example.diplom.entity.Match;
import com.example.diplom.entity.Swipe;
import com.example.diplom.entity.User;
import com.example.diplom.repository.MatchRepository;
import com.example.diplom.repository.SwipeRepository;
import com.example.diplom.repository.UserRepository;
import com.example.diplom.service.MatchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/likes")
public class LikedController {

    private final MatchService matchService;
    private final UserRepository userRepo;

    public LikedController(MatchService matchService, UserRepository userRepo) {
        this.matchService = matchService;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String getLikedUsers(Model model, Principal principal) {
        User currentUser = userRepo.findByUsername(principal.getName()).orElseThrow();
        List<User> incoming = matchService.getUsersWhoLikedMe(currentUser);
        System.out.println("Пользователи, лайкнувшие " + currentUser.getUsername() + ": " + incoming.size());
        model.addAttribute("incomingUsers", incoming);
        return "liked-me";
    }

    @PostMapping("/respond")
    public String respond(@RequestParam Long targetUserId,
                          @RequestParam boolean liked,
                          Principal principal) {
        User currentUser = userRepo.findByUsername(principal.getName()).orElseThrow();
        User targetUser = userRepo.findById(targetUserId).orElseThrow();

        matchService.swipe(currentUser, targetUser, liked);

        return "redirect:/likes";
    }
}
