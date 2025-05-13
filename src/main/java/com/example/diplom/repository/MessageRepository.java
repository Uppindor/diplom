package com.example.diplom.repository;


import com.example.diplom.entity.Match;
import com.example.diplom.entity.Message;
import com.example.diplom.entity.Swipe;
import com.example.diplom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByMatch(Match match);
    List<Message> findTop10ByMatchIdOrderBySentAtDesc(Long matchId);
}
