package com.b6122.ping.service;


import com.b6122.ping.oauth.provider.OAuthProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaverOAuthService {

    public String getNaverAccessToken(String authorizationCode) throws IOException {
        String tokenEndpoint = "https://nid.naver.com/oauth2.0/token";

        // HTTP 연결 설정
        URL url = new URL(tokenEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        connection.setDoOutput(true);

        // 데이터 작성
        String requestBody = "client_id=" + OAuthProperties.NAVER_CLIENT_ID +
                "&client_secret=" + OAuthProperties.NAVER_CLIENT_SECRET +
                "&grant_type=authorization_code" +
                "&code=" + authorizationCode +
                "&state=" + OAuthProperties.NAVER_STATE;

        // 데이터 전송
        try (OutputStream os = connection.getOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
            writer.write(requestBody);
            writer.flush();
        }

        // 응답 처리
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.toString());
                return jsonNode.get("access_token").asText();
            }
        } else {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                return errorResponse.toString(); // Exception handling required...
            }
        }
    }

    public Map<String, Object> getNaverUserInfo(String accessToken) throws IOException {
        String requestEndpoint = "https://openapi.naver.com/v1/nid/me";

        URL url = new URL(requestEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        // Handle CORS issue by allowing requests from the frontend domain
        connection.setRequestProperty("Access-Control-Allow-Origin", "https://your-frontend-domain.com");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> userInfoMap = objectMapper.readValue(response.toString(), Map.class);
                userInfoMap.put("provider", "naver");
                return userInfoMap;
            }
        } else {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(errorResponse.toString(), Map.class); // Exception handling required...
            }
        }
    }
}
