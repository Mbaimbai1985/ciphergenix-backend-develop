package com.authorization.server.service;
import com.authorization.server.model.User;
public interface UserService {
    User getUserByEmail(String email);
    void resetLoginAttempts(String userUuid);
    void updateLoginAttempts(String email);
    void setLastLogin(Long userId);
    void addLoginDevice(Long userId, String deviceName, String client, String ipAddress);
    boolean verifyQrCode(String userUuid, String code);
}