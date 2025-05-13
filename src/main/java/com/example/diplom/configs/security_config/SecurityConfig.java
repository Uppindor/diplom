package com.example.diplom.configs.security_config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // Отключаем CSRF для API
                )
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                // Статические ресурсы и публичные эндпоинты
                                "/", "/registration", "/auth/status", "/login", "/home", "/about",
                                "/images/**", "/contact", "/css/**", "/js/**", "/webjars/**",
                                "/favicon.ico", "/error",

                                // WebSocket и API endpoints
                                "/topic/**", "/app/**", "/ws/**", "/sockjs-node/**",
                                "/api/public/**", "/v3/api-docs/**", "/swagger-ui/**",

                                // OpenRouter API прокси
                                "/api/openrouter/**",

                                // DeepSeek API (если еще используется)
                                "/api/deepseek/**"
                        ).permitAll()

                        .requestMatchers(
                                // Аутентифицированные эндпоинты
                                "/card/**", "/liked-me/**", "/likes/**", "/pass/**",
                                "/match/**", "/chats/**", "/chat/**", "/profile/**",
                                "/respond", "/setting/**", "/addImage/**", "/image/**",
                                "/api/ask/**", "/api/private/**", "/question/**",
                                "/ai/question/**", "/user/**"
                        ).authenticated()

                        .requestMatchers(
                                // Админские эндпоинты
                                "/admin/**", "/actuator/**", "/h2-console/**"
                        ).hasRole("ADMIN")
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/card", true)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/ws/**", "/api/openrouter/**", "/api/deepseek/**",
                                "/api/ask/question/**", "/ai/question/**"
                        )
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions
                                .sameOrigin()
                        )
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}