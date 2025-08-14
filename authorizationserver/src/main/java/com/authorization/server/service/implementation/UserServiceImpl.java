package com.authorization.server.service.implementation;
import com.authorization.server.model.User;
import com.authorization.server.repository.UserRepository;
import com.authorization.server.service.UserService;
import com.authorization.server.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    @Override
    public void resetLoginAttempts(String userUuid) {
        userRepository.resetLoginAttempts(userUuid);
    }

    @Override
    public void updateLoginAttempts(String email) {
        userRepository.updateLoginAttempts(email);
    }

    @Override
    public void setLastLogin(Long userId) {
        userRepository.setLastLogin(userId);
    }

    @Override
    public void addLoginDevice(Long userId, String deviceName, String client, String ipAddress) {
        userRepository.addLoginDevice(userId, deviceName, client, ipAddress);
    }

    @Override
    public boolean verifyQrCode(String userUuid, String code) {
        var user = userRepository.getUserByUuid(userUuid);
        return UserUtils.verifyQrCode(user.getQrCodeSecret(), code);
    }
}