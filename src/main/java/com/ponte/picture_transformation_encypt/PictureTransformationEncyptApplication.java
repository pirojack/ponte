package com.ponte.picture_transformation_encypt;

import com.ponte.picture_transformation_encypt.util.SecretKeyGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class PictureTransformationEncyptApplication {

	public static void main(String[] args) throws Exception {

		Path keyPath = Paths.get("key","secretKey.key");

		if(!Files.exists(keyPath)) SecretKeyGenerator.generateSecretKey("key");

		SpringApplication.run(PictureTransformationEncyptApplication.class, args);
	}

}
