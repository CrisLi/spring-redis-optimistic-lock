package org.chris.demo.redis.lock;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
public class RedisDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(ClientRequest request) {
        return (args) -> {
            request.execute();
        };
    }

    @Bean
    public ProductStore productStore(RedisTemplate<String, String> redisTemplate) {
        return new ProductStore(redisTemplate, 50);
    }

    @Bean
    public ClientRequest clientRequest(ProductStore store) {
        return new ClientRequest(store);
    }

}
