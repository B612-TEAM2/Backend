package com.b6122.ping.service;

import com.b6122.ping.oauth.provider.OAuthProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {

    //프론트에서 전달 받은 인가코드로 카카오 서버로 요청해서 access token 받는 메소드
    //요청 데이터 타입 (Content-type: application/x-www-form-urlencoded;charset=utf-8)

    //네트워크 통신 방법
    //1. HttpURLConnection -> 아래 메소드에서 사용
    //2. RestTemplate(추후 deprecated 예정?)
    //3. HttpClient
    //4. WebClient(Spring WebFlux)
    //5. RestTemplate 기반의 Spring 6.1 RestClient
    public String getKakaoAccessToken(String authorizationCode) throws IOException {
        String tokenEndpoint = "https://kauth.kakao.com/oauth/token";

        // HTTP 연결 설정
        URL url = new URL(tokenEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST"); // post만 가능
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8"); //필수
        connection.setDoOutput(true); //데이터의 전송을 허용, 기본 값 false

        // 데이터 작성, 요청 시 필요한 필수 요소만 포함
        String requestBody = "grant_type=authorization_code" +
                "&client_id=" + OAuthProperties.KAKAO_CLIENT_ID +
                "&redirect_uri" + OAuthProperties.KAKAO_REDIRECT_URI +
                "&code=" + authorizationCode;

        // 데이터 전송
        try (OutputStream os = connection.getOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
            writer.write(requestBody);
            writer.flush();
        }

        // 응답 처리
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 성공적으로 access token을 받은 경우
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.toString());
                System.out.println("jsonNode = " + jsonNode);
                // access token 추출 후 컨트롤러로 전달
                return jsonNode.get("access_token").asText();
            }
        } else {
            // 오류가 발생한 경우
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                return errorResponse.toString(); //예외처리 필요..
            }
        }
    }

    public Map<String, Object> getKakaoUserInfo(String accessToken) throws IOException{
        String requestEndpoint = "https://kapi.kakao.com/v2/user/me";

        URL url = new URL(requestEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST"); //get post 둘다 가능
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8"); //필수
        connection.setRequestProperty("Authorization", "Bearer " + accessToken); //필수
        connection.setDoOutput(true);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 성공적으로 값을 전송받았다면
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(response.toString(), Map.class);
            }
        } else {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(errorResponse.toString(), Map.class); //예외처리 필요..
            }
        }
    }
}
