package com.example.diplom.service;

import com.example.diplom.entity.User;
import com.example.diplom.models.UserProfileDTO;
import com.example.diplom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public void updateProfile(UserProfileDTO dto) {
        User user = getCurrentUser();
        user.setAge(dto.getAge());
        user.setBio(dto.getBio());
        user.setEducation(dto.getEducation());
        user.setWork(dto.getWork());
        user.setHobbies(dto.getHobbies());
        user.setPersonal(dto.getPersonal());
        user.setMusic(dto.getMusic());
        userRepository.save(user);
    }
}