package Group5_pizza.Pizza_GoGo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ====== Xác thực tài khoản ======
    public void saveToken(String token, String username) {
        redisTemplate.opsForValue().set("token:" + token, username, 10, TimeUnit.MINUTES);
    }

    public String getUsernameByToken(String token) {
        return redisTemplate.opsForValue().get("token:" + token);
    }

    public void deleteToken(String token) {
        redisTemplate.delete("token:" + token);
    }

    // ====== Lưu thông tin đăng ký tạm thời ======
    public void savePendingAccount(String token, Map<String, String> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set("pending:" + token, json, 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi khi lưu thông tin tạm vào Redis", e);
        }
    }

    public Map<String, String> getPendingAccount(String token) {
        try {
            String json = redisTemplate.opsForValue().get("pending:" + token);
            if (json == null) return null;
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi khi đọc thông tin tạm từ Redis", e);
        }
    }

    public void deletePendingAccount(String token) {
        redisTemplate.delete("pending:" + token);
    }
}