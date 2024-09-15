package com.ponte.picture_transformation_encypt.service;

import com.ponte.picture_transformation_encypt.modell.Picture;
import com.ponte.picture_transformation_encypt.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PictureService {
    @Autowired
    private PictureRepository pictureRepository;

    public void savePicture(Picture picture) {
        pictureRepository.save(picture);
    }

    public Picture getPicture(Long id) {
        return pictureRepository.findById(id).orElse(null);
    }
    public List<Picture> getPicturesByName(String pictureName) {
        return pictureRepository.findByName(pictureName);
    }
    public List<Picture> getAllPictures() {
        return pictureRepository.findAll();
    }

}
