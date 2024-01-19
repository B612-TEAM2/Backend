package com.b6122.ping.oauth.provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OAuthProperties {

    // Kakao OAuth properties
    @Value("${kakao.client-id}")
    public static String KAKAO_CLIENT_ID;

    @Value("${kakao.client-secret}")
    public static String KAKAO_CLIENT_SECRET;

    @Value("${kakao.redirect-uri}")
    public static String KAKAO_REDIRECT_URI;

    // Google OAuth properties
    @Value("${google.client-id}")
    public static String GOOGLE_CLIENT_ID;

    @Value("${google.client-secret}")
    public static String GOOGLE_CLIENT_SECRET;

    @Value("${google.redirect-uri}")
    public static String GOOGLE_REDIRECT_URI;

    // Naver OAuth properties (if applicable)
    @Value("${naver.client-id}")
    public static String NAVER_CLIENT_ID;

    @Value("${naver.client-secret}")
    public static String NAVER_CLIENT_SECRET;

    @Value("${naver.redirect-uri}")
    public static String NAVER_REDIRECT_URI;

    // Add any other OAuth provider properties as needed

    // Constructors, getters, and setters can be added if required
}
