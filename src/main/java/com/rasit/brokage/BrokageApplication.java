package com.rasit.brokage;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class BrokageApplication {

    public static void main(String[] args) {
        try {
            log.info("Starting BrokageApplication...");
            SpringApplication.run(BrokageApplication.class, args);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> log.info("Application has been closed")));
        } catch (ValidationException e) {
            log.error(ExceptionUtils.getMessage(e));
        }
    }
}
