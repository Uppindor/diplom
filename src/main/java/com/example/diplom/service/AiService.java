package com.example.diplom.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AiService {
    private final WebClient webClient;
    private static final String API_KEY = "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";

    public AiService(WebClient.Builder webClientBuilder) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("OpenRouter API key is not set");
        }
        this.webClient = webClientBuilder
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Authorization", "Bearer " + API_KEY)
                .defaultHeader("HTTP-Referer", "http://localhost:8080")
                .defaultHeader("X-Title", "DiplomApp")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .wiretap(true)
                ))
                .build();
    }

    public String generateQuestion(String context) {
        try {
            log.info("Sending request to OpenRouter API with context length: {}", context.length());

            // Формируем запрос специально для Mistral-7B-Instruct
            Map<String, Object> requestBody = Map.of(
                    "model", "mistralai/mistral-7b-instruct",
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", "Сгенерируй вопрос на основе следующего контекста: " + context
                            )
                    ),
                    "temperature", 0.7,
                    "max_tokens", 256
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> {
                        log.error("OpenRouter API error. Status: {}, Headers: {}",
                                resp.statusCode(), resp.headers().asHttpHeaders());
                        return resp.bodyToMono(String.class)
                                .defaultIfEmpty("No error body")
                                .flatMap(body -> {
                                    log.error("API error response: {}", body);
                                    return Mono.error(new RuntimeException(
                                            "OpenRouter error: " + resp.statusCode() + " - " + body));
                                });
                    })
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(30));

            // Обработка ответа для формата Mistral
            if (response == null) {
                log.error("Null response from API");
                throw new RuntimeException("Empty API response");
            }

            log.debug("Full API response: {}", response);

            // Упрощенная проверка структуры ответа
            String content = Optional.ofNullable(response)
                    .map(r -> (List<Map<String, Object>>) r.get("choices"))
                    .filter(choices -> !choices.isEmpty())
                    .map(choices -> choices.get(0))
                    .map(choice -> (Map<String, Object>) choice.get("message"))
                    .map(message -> (String) message.get("content"))
                    .orElseThrow(() -> {
                        log.error("Invalid response structure: {}", response);
                        return new RuntimeException("Invalid API response structure");
                    });

            if (content.isBlank()) {
                throw new RuntimeException("Empty content in response");
            }

            return content.trim();

        } catch (Exception e) {
            log.error("Failed to generate question", e);
            return "Не удалось сгенерировать вопрос. Ошибка: " + e.getMessage();
        }
    }
}