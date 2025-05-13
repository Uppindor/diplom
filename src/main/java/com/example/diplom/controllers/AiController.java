package com.example.diplom.controllers;


import com.example.diplom.entity.Match;
import com.example.diplom.entity.Message;
import com.example.diplom.entity.User;
import com.example.diplom.repository.MatchRepository;
import com.example.diplom.repository.MessageRepository;
import com.example.diplom.repository.UserRepository;
import com.example.diplom.service.AiService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;



import java.security.Principal;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ask")
@Slf4j
public class AiController {
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final AiService aiService;

    public AiController(UserRepository userRepository,
                        MatchRepository matchRepository,
                        MessageRepository messageRepository,
                        AiService aiService) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.messageRepository = messageRepository;
        this.aiService = aiService;
    }

    @GetMapping("/match-id/{username}")
    public ResponseEntity<Map<String, Long>> getMatchId(
            @PathVariable String username,
            Principal principal) {

        try {
            log.info("Запрос matchId для {} от пользователя {}", username, principal.getName());

            User current = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Текущий пользователь не найден"));

            User target = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Целевой пользователь не найден"));

            Match match = matchRepository.findByUser1AndUser2(current, target)
                    .or(() -> matchRepository.findByUser1AndUser2(target, current))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Матч между пользователями не найден"));

            return ResponseEntity.ok(Map.of("matchId", match.getId()));

        } catch (ResponseStatusException e) {
            log.error("Ошибка поиска матча", e);
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    @GetMapping("/question/{matchId}")
    public ResponseEntity<Map<String, String>> generateQuestion(
            @PathVariable Long matchId,
            Principal principal) {

        try {
            log.info("Генерация вопроса для matchId {} от пользователя {}",
                    matchId, principal.getName());

            List<Message> messages = messageRepository
                    .findTop10ByMatchIdOrderBySentAtDesc(matchId);

            if (messages.isEmpty()) {
                return ResponseEntity.ok(
                        Map.of("question", "Недостаточно сообщений для генерации вопроса."));
            }

            String context = messages.stream()
                    .sorted(Comparator.comparing(Message::getSentAt))
                    .map(m -> m.getSender().getUsername() + ": " + m.getContent())
                    .collect(Collectors.joining("\n"));

            String question = aiService.generateQuestion(context);

            return ResponseEntity.ok(Map.of("question", question));

        } catch (Exception e) {
            log.error("Ошибка генерации вопроса", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("question", "Ошибка при генерации вопроса"));
        }
    }
}