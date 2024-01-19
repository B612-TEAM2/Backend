package com.b6122.ping.oauth.provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.math.BigInteger;
@Component
public class OAuthProperties {

    public static String NAVER_CLIENT_ID = "HT4vaEYy1mRjQXVadpwE";
    public static String NAVER_CLIENT_SECRET = "cDBX96chK3";

    public static String NAVER_STATE = new BigInteger(130, new SecureRandom()).toString(32);

}
