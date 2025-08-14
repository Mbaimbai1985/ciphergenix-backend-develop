package com.discovery.repository;
import com.discovery.model.User;
public interface UserRepository {
    User getUserByUsername(String username);
}