package com.authorization.server.domain;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
public class Analyzer {
    private static UserAgentAnalyzer INSTANCE;
    public static UserAgentAnalyzer getInstance() {
        if(INSTANCE == null) {
            INSTANCE = UserAgentAnalyzer
                    .newBuilder()
                    .hideMatcherLoadStats()
                    .withCache(10000)
                    .build();
        }
        return INSTANCE;
    }
}