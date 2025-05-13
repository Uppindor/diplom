package com.example.diplom.repository;

import com.example.diplom.entity.Match;
import com.example.diplom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    // Найти матч по пользователям user1 и user2
    Optional<Match> findByUser1AndUser2(User user2, User user1);
    // Найти все матчи для конкретного пользователя (где он является или user1, или user2)
    List<Match> findByUser1OrUser2(User user1, User user2);

}
