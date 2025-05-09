package com.b21dccn449.quanlytour;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
public class QuanlytourApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanlytourApplication.class, args);
    }
}
