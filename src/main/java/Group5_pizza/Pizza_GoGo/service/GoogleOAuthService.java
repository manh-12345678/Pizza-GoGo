package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.config.GoogleProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class GoogleOAuthService {

    private final GoogleProperties googleProperties;
    private final RestTemplate restTemplate;

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    public GoogleOAuthService(GoogleProperties googleProperties) {
        this.googleProperties = googleProperties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * URL chuyển hướng người dùng đến Google login
     */
    public String getAuthorizationUrl() {
        return UriComponentsBuilder.fromUriString(AUTH_URL)
                .queryParam("client_id", googleProperties.getClientId())
                .queryParam("redirect_uri", googleProperties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email") // ⚠ phải cách nhau bằng space
                .queryParam("access_type", "offline")
                .build()
                .toUriString();
    }

    /**
     * Lấy access token từ code trả về
     */
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("client_id", googleProperties.getClientId());
        map.add("client_secret", googleProperties.getClientSecret());
        map.add("redirect_uri", googleProperties.getRedirectUri());
        map.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(TOKEN_URL, request, Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("Failed to retrieve access token from Google");
        }

        return response.get("access_token").toString();
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        String url = USER_INFO_URL + "?access_token=" + accessToken;
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("email")) {
            throw new RuntimeException("Failed to retrieve user info from Google");
        }

        return response;
    }
}
