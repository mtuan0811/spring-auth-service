package com.example.userservice.services.impl;

import com.example.userservice.domain.dto.JwtDto;
import com.example.userservice.domain.dto.TokenRefreshDto;
import com.example.userservice.domain.models.Role;
import com.example.userservice.domain.models.Session;
import com.example.userservice.domain.models.User;
import com.example.userservice.domain.models.enums.ERole;
import com.example.userservice.domain.payload.request.LoginRequest;
import com.example.userservice.domain.payload.request.SignupRequest;
import com.example.userservice.domain.payload.request.TokenRefreshRequest;
import com.example.userservice.exception.ConflictException;
import com.example.userservice.exception.NotFoundException;
import com.example.userservice.exception.TokenRefreshException;
import com.example.userservice.domain.models.RefreshToken;
import com.example.userservice.repositories.RoleRepository;
import com.example.userservice.repositories.SessionUserRepository;
import com.example.userservice.repositories.UserRepository;
import com.example.userservice.repositories.impl.RefreshTokenRepositoryImpl;
import com.example.userservice.security.jwt.JwtUtils;
import com.example.userservice.security.services.impl.UserDetailsImpl;
import com.example.userservice.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {
    @Value("${security.jwt.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepositoryImpl refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SessionUserRepository sessionUserRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordEncoder encoder;

    public RefreshToken createRefreshToken(String userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userRepository.findById(userId).get().getId());
        refreshToken.setExpiryDate(Date.from(Instant.now().plusMillis(refreshTokenDurationMs)));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshTokenRepository.createRefeshToken(refreshToken);
        return refreshToken;
    }

    @Override
    public TokenRefreshDto refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        if(Objects.isNull(refreshToken) || refreshToken.isEmpty()) {
            throw new NotFoundException("Refresh Token is empty!");
        }
         String userId = findByRefreshToken(refreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUserId).orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token is not in database!"));
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User is not existed"));
        return new TokenRefreshDto(jwtUtils.generateTokenFromUsername(user.getUsername(), refreshToken), refreshToken);
    }

    @Override
    public JwtDto signUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        RefreshToken refreshToken = createRefreshToken(userDetails.getId());
        String jwt = jwtUtils.generateJwtToken(authentication, refreshToken.getToken());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        Session session = new Session(userDetails.getId(), refreshToken.getToken(), 120L);
        sessionUserRepository.save(session);
        return new JwtDto(jwt, refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    @Override
    @Transactional
    public void registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new ConflictException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ConflictException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void logoutUser() {
        Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(principle.toString());
        if (!principle.toString().equals("anonymousUser")) {
            String userId = ((UserDetailsImpl) principle).getId();
            System.out.println(userId);
            refreshTokenRepository.deleteTokenByUser(userId);
            Optional<Session> session = sessionUserRepository.findSessionByUserId(userId);
            session.ifPresent(value -> sessionUserRepository.delete(value));
        }
    }

    public Optional<RefreshToken> findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefeshToken(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Date.from(Instant.now())) < 0) {
            refreshTokenRepository.deleteToken(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
