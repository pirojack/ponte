package com.ponte.picture_transformation_encypt.repository;

import com.ponte.picture_transformation_encypt.modell.Picture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PictureRepository extends JpaRepository<Picture, Long> {
    List<Picture> findByName(String name);

}
