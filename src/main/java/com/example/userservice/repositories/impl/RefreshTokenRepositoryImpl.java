package com.example.userservice.repositories.impl;

import com.example.userservice.domain.models.RefreshToken;
import com.example.userservice.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Optional<RefreshToken> findByRefeshToken(String refreshToken) {
        Query query = new Query();
        query.addCriteria(Criteria.where("token").is(refreshToken));
        RefreshToken findRefreshToken = mongoTemplate.findOne(query, RefreshToken.class);
        if(Objects.isNull(findRefreshToken)) return Optional.empty();
        return Optional.of(findRefreshToken);
    }

    @Override
    public void createRefeshToken(RefreshToken refreshToken) {
        mongoTemplate.save(refreshToken);
    }

    @Override
    public long deleteToken(RefreshToken refreshToken) {
        return mongoTemplate.remove(refreshToken).getDeletedCount();
    }

    @Override
    public long deleteTokenByUser(String userId) {
        return mongoTemplate.remove(new Query(Criteria.where("userId").is(userId)), RefreshToken.class).getDeletedCount();
    }
}
