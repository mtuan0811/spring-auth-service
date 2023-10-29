package com.example.userservice.repositories;

import com.example.userservice.domain.models.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionUserRepository extends CrudRepository<Session, String> {
    public Optional<Session> findSessionByRefreshToken(String refreshToken);
    public Optional<Session> findSessionByUserId(String userId);
}
