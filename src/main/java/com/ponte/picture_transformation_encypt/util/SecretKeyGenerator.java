package com.ponte.picture_transformation_encypt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class SecretKeyGenerator {
    static Logger LOG = LoggerFactory.getLogger(SecretKeyGenerator.class);
    public static void generateSecretKey(String keyPath) throws Exception {

        if (keyPath == null ) {
            LOG.error("No file path provided.");
            return;
        }

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // 128-bit key
        SecretKey secretKey = keyGen.generateKey();

        // Store the secret key in a file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(keyPath+"/secretKey.key"))) {
            oos.writeObject(secretKey);
            LOG.info("Secretkey succesfull generated!");
        }
    }
}
