package com.discovery.repository.implementation;
import com.discovery.exception.ApiException;
import com.discovery.model.User;
import com.discovery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import static com.discovery.query.UserQuery.SELECT_USER_BY_USERNAME_QUERY;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final JdbcClient jdbc;

    @Override
    public User getUserByUsername(String username) {
            try {
                return jdbc.sql(SELECT_USER_BY_USERNAME_QUERY).param("username", username).query(User.class).single();
            } catch (EmptyResultDataAccessException exception) {
                log.error(exception.getMessage());
                throw new ApiException(String.format("No user found by username %s", username));
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException("An error occurred. Please try again.");
            }
    }
}