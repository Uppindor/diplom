package com.example.diplom.controllers;

import com.example.diplom.entity.Image;
import com.example.diplom.entity.User;
import com.example.diplom.models.UserProfileDTO;
import com.example.diplom.service.ImageService;
import com.example.diplom.service.UserProfileService;
import com.example.diplom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserProfileController {

    @Value("${uploadDir}")
    private String uploadDir;
    @Autowired
    private UserService userService;
    private final UserProfileService userProfileService;

    @Autowired
    private final ImageService imageService;


    @GetMapping("/profile")
    public String getProfile(Model model) {
        User user = userProfileService.getCurrentUser();
        UserProfileDTO dto = new UserProfileDTO();
        dto.setAge(user.getAge());
        dto.setBio(user.getBio());
        dto.setEducation(user.getEducation());
        dto.setWork(user.getWork());
        dto.setHobbies(user.getHobbies());
        dto.setPersonal(user.getPersonal());
        dto.setMusic(user.getMusic());

        model.addAttribute("users", List.of(user));
        model.addAttribute("userProfileDto", dto);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute UserProfileDTO dto, @RequestParam("image") MultipartFile file, Principal principal) throws IOException, NoSuchAlgorithmException {
        byte[] fileImage =  file.getInputStream().readAllBytes();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(fileImage);

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }

        String fileName = uploadDir + hexString.toString();

        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        fileOutputStream.write(file.getInputStream().readAllBytes());
        Image image = new Image();
        image.setUrl(fileName);
        image.setMain(true);
        String username = principal.getName();
        User currentUser = userService.findByUsername(username);
        image.setUser(currentUser);
        imageService.saveImage(image);
        userProfileService.updateProfile(dto);
        return "redirect:/profile";
    }


    @GetMapping(value = "/image/{id}", produces = "image/bmp")
    public @ResponseBody byte[] getImage(@PathVariable Long id) throws IOException {
        Image image = imageService.getImage(id);
        InputStream in = new FileInputStream(image.getUrl());
        return IOUtils.toByteArray(in);
    }
}