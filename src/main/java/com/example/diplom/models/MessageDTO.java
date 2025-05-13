package com.example.diplom.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String sender;
    private String content;
    private String sentAt; // как строка или LocalDateTime

    // Конструктор
    public MessageDTO(String sender, String content, LocalDateTime sentAt) {
        this.sender = sender;
        this.content = content;
        // Форматируем в "часы:минуты"
        this.sentAt = sentAt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}