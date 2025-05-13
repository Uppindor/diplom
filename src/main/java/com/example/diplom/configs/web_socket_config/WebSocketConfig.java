package com.example.diplom.configs.web_socket_config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // должен совпадать с SockJS на клиенте
                .setAllowedOriginPatterns("*")
                .withSockJS(); // важно для поддержки браузеров без нативного WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app"); // для @MessageMapping
        registry.enableSimpleBroker("/topic", "/queue"); // поддержка "/queue/reply"
        registry.setUserDestinationPrefix("/user"); // критично для convertAndSendToUser
    }
}