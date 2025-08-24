package com.moim.payment.domain.Usr;

public enum Provider {
    GOOGLE("google"),KAKAO("kakao");

    private final String provider;

    Provider(String provider) {
        this.provider = provider;
    }

    public static Provider of(String provider) {
        switch (provider) {
            case "google" :
                return Provider.GOOGLE;
            case "kakao" :
                return Provider.KAKAO;
            default:
                return null;
        }
    }
}
