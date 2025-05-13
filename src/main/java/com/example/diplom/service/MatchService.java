package com.example.diplom.service;

import com.example.diplom.entity.Match;
import com.example.diplom.entity.Swipe;
import com.example.diplom.entity.User;
import com.example.diplom.repository.MatchRepository;
import com.example.diplom.repository.SwipeRepository;
import com.example.diplom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    public void swipe(User fromUser, User toUser, boolean liked) {
        Optional<Swipe> existingSwipe = swipeRepository.findByFromUserAndToUser(fromUser, toUser);

        if (existingSwipe.isPresent()) {
            Swipe swipe = existingSwipe.get();
            swipe.setLiked(liked);
            swipe.setCreatedAt(LocalDateTime.now());
            swipeRepository.save(swipe);
        } else {
            Swipe swipe = new Swipe();
            swipe.setFromUser(fromUser);  // was: setUser1
            swipe.setToUser(toUser);      // was: setUser2
            swipe.setLiked(liked);
            swipe.setCreatedAt(LocalDateTime.now());
            swipeRepository.save(swipe);
        }

        checkForMatch(fromUser, toUser);
    }

    private void checkForMatch(User fromUser, User toUser) {
        Optional<Swipe> swipe1 = swipeRepository.findByFromUserAndToUser(fromUser, toUser);
        Optional<Swipe> swipe2 = swipeRepository.findByFromUserAndToUser(toUser, fromUser);

        if (swipe1.isPresent() && swipe2.isPresent()) {
            if (swipe1.get().isLiked() && swipe2.get().isLiked()) {
                Match match = new Match();
                match.setUser1(fromUser);
                match.setUser2(toUser);
                match.setMatchedAt(LocalDateTime.now());
                matchRepository.save(match);
            }
        }
    }

    public User getNextUnswipedUser(User currentUser) {
        List<User> allUsers = userRepository.findAll();

        // Получаем ID пользователей, которых текущий пользователь уже свайпнул
        List<User> swipedUsers = swipeRepository.findByFromUser(currentUser)
                .stream()
                .map(Swipe::getToUser)
                .collect(Collectors.toList());

        return allUsers.stream()
                .filter(user -> !user.equals(currentUser))
                .filter(user -> !swipedUsers.contains(user))
                .findFirst()
                .orElse(null);
    }

    public List<User> getUsersWhoLikedMe(User currentUser) {
        List<Swipe> swipes = swipeRepository.findByToUserAndLikedTrue(currentUser);
        System.out.println("Входящих свайпов на " + currentUser.getUsername() + ": " + swipes.size());
        swipes.forEach(s -> System.out.println("От: " + s.getFromUser().getUsername()));
        return swipes.stream().map(Swipe::getFromUser).collect(Collectors.toList());
    }

    public List<Match> getMatches(User user) {
        return matchRepository.findByUser1OrUser2(user, user);
    }
}
