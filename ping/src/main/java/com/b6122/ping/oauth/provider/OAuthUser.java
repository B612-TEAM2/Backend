package com.b6122.ping.oauth.provider;

public interface OAuthUser {
    String getProviderId();
    String getProvider();
    String getName();
}
