package com.listme.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {



    @org.springframework.beans.factory.annotation.Value("${firebase.storage.bucket}")
    private String storageBucket;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                // Use ClassPathResource ao invés de FileInputStream
                Resource resource = new ClassPathResource("ipval1.json");
                InputStream serviceAccount = resource.getInputStream();


                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(storageBucket)
                        .build();

                return FirebaseApp.initializeApp(options);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao configurar Firebase: " + e.getMessage(), e);
            }
        }
        return FirebaseApp.getInstance();
    }
}