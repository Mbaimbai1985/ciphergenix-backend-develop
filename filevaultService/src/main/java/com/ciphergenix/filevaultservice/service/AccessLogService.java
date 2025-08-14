package com.ciphergenix.filevaultservice.service;

import com.ciphergenix.filevaultservice.model.AccessLog;
import com.ciphergenix.filevaultservice.repository.AccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private AccessLogRepository logRepository = new AccessLogRepository() {
        @Override
        public void flush() {

        }

        @Override
        public <S extends AccessLog> S saveAndFlush(S entity) {
            return null;
        }

        @Override
        public <S extends AccessLog> List<S> saveAllAndFlush(Iterable<S> entities) {
            return List.of();
        }

        @Override
        public void deleteAllInBatch(Iterable<AccessLog> entities) {

        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Long> longs) {

        }

        @Override
        public void deleteAllInBatch() {

        }

        @Override
        public AccessLog getOne(Long aLong) {
            return null;
        }

        @Override
        public AccessLog getById(Long aLong) {
            return null;
        }

        @Override
        public AccessLog getReferenceById(Long aLong) {
            return null;
        }

        @Override
        public <S extends AccessLog> List<S> findAll(Example<S> example) {
            return List.of();
        }

        @Override
        public <S extends AccessLog> List<S> findAll(Example<S> example, Sort sort) {
            return List.of();
        }

        @Override
        public <S extends AccessLog> List<S> saveAll(Iterable<S> entities) {
            return List.of();
        }

        @Override
        public List<AccessLog> findAll() {
            return List.of();
        }

        @Override
        public List<AccessLog> findAllById(Iterable<Long> longs) {
            return List.of();
        }

        @Override
        public <S extends AccessLog> S save(S entity) {
            return null;
        }

        @Override
        public Optional<AccessLog> findById(Long aLong) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Long aLong) {
            return false;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Long aLong) {

        }

        @Override
        public void delete(AccessLog entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends Long> longs) {

        }

        @Override
        public void deleteAll(Iterable<? extends AccessLog> entities) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public List<AccessLog> findAll(Sort sort) {
            return List.of();
        }

        @Override
        public Page<AccessLog> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public <S extends AccessLog> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends AccessLog> Page<S> findAll(Example<S> example, Pageable pageable) {
            return null;
        }

        @Override
        public <S extends AccessLog> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends AccessLog> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends AccessLog, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }
    };

    public void log(String fileName, String action, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        AccessLog log = AccessLog.builder()
                .fileName(fileName)
                .action(action)
                .ipAddress(ip)
                .timestamp(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }
}
