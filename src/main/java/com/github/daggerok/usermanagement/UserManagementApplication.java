package com.github.daggerok.usermanagement;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Log4j2
@ComponentScan(basePackageClasses = UserManagementApplication.class)
public class UserManagementApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        LocalDateTime startTime = LocalDateTime.now();
        new AnnotationConfigApplicationContext(UserManagementApplication.class)
                .getBean(HttpServer.class);
        log.info("Server started in {} seconds.",
                 Duration.between(startTime, LocalDateTime.now()).toMillis() / 1000.0);
    }
}
