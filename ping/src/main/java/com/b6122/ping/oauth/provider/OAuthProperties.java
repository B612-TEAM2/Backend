package com.b6122.ping.oauth.provider;

import java.security.SecureRandom;
import java.math.BigInteger;

public interface OAuthProperties {

    String NAVER_CLIENT_ID = "HT4vaEYy1mRjQXVadpwE";
    String NAVER_CLIENT_SECRET = "cDBX96chK3";
    String NAVER_STATE = new BigInteger(130, new SecureRandom()).toString(32);
    String NAVER_REDIRECT_URI = "http://localhost:3000/authnaver";
    String KAKAO_CLIENT_ID = "4294c81106f19588526f3a34cb2b4356";
    String KAKAO_REDIRECT_URI =  "http://localhost:3000/authkakao";
    String GOOGLE_CLIENT_ID = "182133073202-g6sdd3ih0rpdlnjc14akqa1uj23ndbvh.apps.googleusercontent.com";
    String GOOGLE_REDIRECT_URI = "http://localhost:3000/authgoogle";
    String GOOGLE_CLIENT_SECRET = "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";

}
