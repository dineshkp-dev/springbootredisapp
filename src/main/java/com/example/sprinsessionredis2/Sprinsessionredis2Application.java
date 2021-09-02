package com.example.sprinsessionredis2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableRedisHttpSession
public class Sprinsessionredis2Application {

    public static void main(String[] args) {
        SpringApplication.run(Sprinsessionredis2Application.class, args);
    }

}
