package com.example.diplom.repository;

import com.example.diplom.entity.Swipe;
import com.example.diplom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe, Long> {
    // Найти свайп по пользователю из и в
    Optional<Swipe> findByFromUserAndToUser(User fromUser, User toUser);
    List<Swipe> findByFromUser (User user);
    // Получить все свайпы, где пользователь является получателем (toUser) и лайкнул
    List<Swipe> findByToUserAndLikedTrue(User toUser);
}
