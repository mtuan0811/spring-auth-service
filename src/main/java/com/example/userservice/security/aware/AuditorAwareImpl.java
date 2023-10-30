package com.example.userservice.security.aware;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if(username == null || username.equals("anonymousUser")) return Optional.of("guest");
        return Optional.of(username);
    }
}