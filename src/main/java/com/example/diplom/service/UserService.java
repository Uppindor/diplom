package com.example.diplom.service;

import com.example.diplom.entity.Chat;
import com.example.diplom.entity.Match;
import com.example.diplom.entity.Message;
import com.example.diplom.entity.User;
import com.example.diplom.repository.MatchRepository;
import com.example.diplom.repository.MessageRepository;
import com.example.diplom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/*todo сделать другую модель юзера*/
@Service
public class UserService implements UserDetailsService {

    /*private BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        super();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }*/

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList() // Без ролей
        );
    }

    public User findByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }

    public List<Chat> getChatsForUser(User user) throws UsernameNotFoundException {

        List<Chat> chats = new ArrayList<>();
        List<Match> matches = matchRepository.findByUser1OrUser2(user,user);
        for( Match match : matches )
        {
            List<Message> messages = messageRepository.findByMatch(match);

            User interlocutor = match.getUser1().equals(user) ? match.getUser2() : match.getUser1();

            // Создаём объект Chat
            Chat chat = new Chat();
            chat.setUser2(interlocutor);
            chat.setMessages(messages);

            // Добавляем в список
            chats.add(chat);
        }

        return chats;

    }

    public Match findMatchBetweenUsers(User user1, User user2) {
        return matchRepository.findByUser1AndUser2(user1, user2)
                .orElse(matchRepository.findByUser1AndUser2(user2, user1)
                        .orElse(null));
    }
}