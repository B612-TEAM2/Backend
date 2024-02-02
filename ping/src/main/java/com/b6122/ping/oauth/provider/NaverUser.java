package com.b6122.ping.oauth.provider;

import java.util.Map;

public class NaverUser implements OAuthUser {

    private Map<String, Object> attribute;

    public NaverUser(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProviderId() {
        return (String)attribute.get("providerId");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getName() {
        return (String)attribute.get("username");
    }

}
