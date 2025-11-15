package Group5_pizza.Pizza_GoGo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ====== Xác thực tài khoản ======
    public void saveToken(@NonNull String token, @NonNull String username) {
        redisTemplate.opsForValue().set("token:" + token, username, 10, TimeUnit.MINUTES);
    }

    public String getUsernameByToken(@NonNull String token) {
        return redisTemplate.opsForValue().get("token:" + token);
    }

    public void deleteToken(@NonNull String token) {
        redisTemplate.delete("token:" + token);
    }

    // ====== Lưu thông tin đăng ký tạm thời ======
    public void savePendingAccount(@NonNull String token, @NonNull Map<String, String> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            if (json != null) {
                redisTemplate.opsForValue().set("pending:" + token, json, 10, TimeUnit.MINUTES);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi khi lưu thông tin tạm vào Redis", e);
        }
    }

    public Map<String, String> getPendingAccount(@NonNull String token) {
        try {
            String json = redisTemplate.opsForValue().get("pending:" + token);
            if (json == null) return null;
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi khi đọc thông tin tạm từ Redis", e);
        }
    }

    public void deletePendingAccount(@NonNull String token) {
        redisTemplate.delete("pending:" + token);
    }

    // ====== Remember Me Token ======
    public void saveRememberMeToken(@NonNull String token, @NonNull String username) {
        // Lưu token trong 30 ngày
        redisTemplate.opsForValue().set("remember:" + token, username, 30, TimeUnit.DAYS);
    }

    public String getUsernameByRememberMeToken(@NonNull String token) {
        return redisTemplate.opsForValue().get("remember:" + token);
    }

    public void deleteRememberMeToken(@NonNull String token) {
        redisTemplate.delete("remember:" + token);
    }
}
