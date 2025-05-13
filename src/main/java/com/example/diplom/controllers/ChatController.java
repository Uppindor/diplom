package com.example.diplom.controllers;

import com.example.diplom.entity.Chat;
import com.example.diplom.entity.Match;
import com.example.diplom.entity.Message;
import com.example.diplom.entity.User;
import com.example.diplom.models.ChatMessage;
import com.example.diplom.models.MessageDTO;
import com.example.diplom.repository.MatchRepository;
import com.example.diplom.repository.MessageRepository;
import com.example.diplom.repository.UserRepository;
import com.example.diplom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired
    private UserService userService;
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @GetMapping("/chat/{username}")
    public String getChatPage(@PathVariable String username, Principal principal, Model model) {
        // Получаем текущего пользователя
        String currentUsername = principal.getName();
        User currentUser = userService.findByUsername(currentUsername);

        // Находим пользователя, с которым текущий пользователь общается
        User chatUser = userService.findByUsername(username);
        if (chatUser == null) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }

        // Получаем список чатов для текущего пользователя
        List<Chat> chatList = userService.getChatsForUser(currentUser);

        // Находим чат с нужным пользователем (если он есть)
        Chat chat = chatList.stream()
                .filter(c -> c.getUser2().equals(chatUser))
                .findFirst()
                .orElse(new Chat(chatUser, List.of()));  // Если чата нет, создаем пустой

        // Передаем данные в модель
        model.addAttribute("chatUser", chatUser.getUsername());
        model.addAttribute("chatMessages", chat.getMessages());
        model.addAttribute("currentUsername", principal.getName()); // 👈 ДОБАВЛЕНО

        return "chat";  // Имя файла шаблона чата (например, chat.html)
    }

    @GetMapping("/chats")
    @ResponseBody
    public Map<String, List<MessageDTO>> getUsersAsJson(Principal principal) {
        String username = principal.getName();
        User currentUser = userService.findByUsername(username);

        List<Chat> chatList = userService.getChatsForUser(currentUser);

        Map<String, List<MessageDTO>> mapChat = new HashMap<>();

        for (Chat chat : chatList) {
            String otherUsername = chat.getUser2().getUsername();

            List<MessageDTO> messageDTOs = chat.getMessages().stream()
                    .map(m -> new MessageDTO(m.getSender().getUsername(), m.getContent(), m.getSentAt()))
                    .collect(Collectors.toList());

            mapChat.put(otherUsername, messageDTOs);
        }

        return mapChat;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        String senderUsername = principal.getName();
        User sender = userService.findByUsername(senderUsername);
        User recipient = userService.findByUsername(chatMessage.getRecipient());

        if (sender == null || recipient == null) return;

        // Находим или создаем Match между пользователями
        Match match = matchRepository.findByUser1AndUser2(sender, recipient)
                .or(() -> matchRepository.findByUser1AndUser2(recipient, sender))
                .orElseGet(() -> {
                    Match m = new Match();
                    m.setUser1(sender);
                    m.setUser2(recipient);
                    m.setMatchedAt(LocalDateTime.now());
                    return matchRepository.save(m);
                });

        // Сохраняем сообщение
        Message message = new Message();
        message.setMatch(match);
        message.setSender(sender);
        message.setContent(chatMessage.getContent());
        message.setSentAt(LocalDateTime.now());
        messageRepository.save(message);

        // Отправляем получателю по WebSocket
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/reply",
                new ChatMessage(
                        chatMessage.getContent(),
                        senderUsername,
                        recipient.getUsername(),
                        message.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm"))
                )
        );
    }

    @GetMapping("/context/{matchId}")
    public ResponseEntity<Map<String, String>> getChatContext(
            @PathVariable Long matchId,
            Principal principal) {

        List<Message> messages = messageRepository.findTop10ByMatchIdOrderBySentAtDesc(matchId);

        if (messages.isEmpty()) {
            return ResponseEntity.ok(Map.of("context", "Нет сообщений в чате"));
        }

        String context = messages.stream()
                .sorted(Comparator.comparing(Message::getSentAt))
                .map(m -> m.getSender().getUsername() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        return ResponseEntity.ok(Map.of("context", context));
    }


}
