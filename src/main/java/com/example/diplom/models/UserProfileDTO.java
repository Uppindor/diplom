package com.example.diplom.models;

import com.example.diplom.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private Integer age;
    private String bio;
    private String education;
    private String work;
    private String hobbies;
    private String personal;
    private String music;

}