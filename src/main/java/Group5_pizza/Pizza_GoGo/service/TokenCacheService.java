package Group5_pizza.Pizza_GoGo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private final StringRedisTemplate redisTemplate;

    public void saveToken(String token, String username) {
        redisTemplate.opsForValue().set(token, username, 10, TimeUnit.MINUTES);
    }

    public String getUsernameByToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public void deleteToken(String token) {
        redisTemplate.delete(token);
    }
}