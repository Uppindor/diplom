/*
package com.example.SexMachine.configs.security_config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@RestController
@RequestMapping("/auth")
public class VkConfig {

    @GetMapping("/login/vk")
    public String redirectToVkLogin() {
        String clientId = "52798948";
        String redirectUri = "https://8536-188-168-215-162.ngrok-free.app";
        String scope = "email";
        String authUrl = "https://oauth.vk.com/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + scope;
        return "redirect:" + authUrl;
    }

    @GetMapping("/callback")
    public String handleVkCallback(@RequestParam("code") String code, Model model) {
        String clientId = "52798948";
        String clientSecret = "quxe9piOmyWDy7qna4ZZ";
        String redirectUri = "http://localhost:8000/auth/callback";

        String tokenUrl = "https://oauth.vk.com/access_token" +
                "?client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&code=" + code;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(tokenUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                Map<String, Object> tokenResponse = new ObjectMapper().readValue(response.getBody(), Map.class);
                String accessToken = (String) tokenResponse.get("access_token");
                String userId = tokenResponse.get("user_id").toString();

                String userInfoUrl = "https://api.vk.com/method/users.get" +
                        "?user_ids=" + userId +
                        "&fields=first_name,last_name,email" +
                        "&access_token=" + accessToken +
                        "&v=5.131";

                ResponseEntity<String> userInfoResponse = restTemplate.getForEntity(userInfoUrl, String.class);
                if (userInfoResponse.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> userInfo = new ObjectMapper().readValue(userInfoResponse.getBody(), Map.class);
                    model.addAttribute("userInfo", userInfo);
                    return "profile"; // Успех
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "error"; // Ошибка
    }
}
*/
