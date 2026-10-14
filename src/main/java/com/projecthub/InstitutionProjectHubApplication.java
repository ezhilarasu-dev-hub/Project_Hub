package com.projecthub;

import com.projecthub.config.DirectoryInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InstitutionProjectHubApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(InstitutionProjectHubApplication.class);
        app.addInitializers(new DirectoryInitializer());
        app.run(args);
    }
}
