package com.example.diplom.service;


import com.example.diplom.entity.Image;
import com.example.diplom.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    public void saveImage(Image image) throws IOException {

        imageRepository.save(image);
    }

    public Image getImage(Long id) throws IOException {
        return  imageRepository.getReferenceById(id);
    }

    public List<Image> getAll()
    {
        return imageRepository.findAll();
    }

    public void deleteImage(Long id) {imageRepository.deleteById(id);}
}