package com.example.userservice.security.jwt;

import com.example.userservice.security.services.impl.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${security.jwt.jwtSecret}")
    private String jwtSecret;

    @Value("${security.jwt.jwtExpirationMs}")
    private int jwtExpirationMs;

    private final String accessToken = "access_token";
    private final String refreshToken = "refresh_token";

    public String generateJwtToken(Authentication authentication, String refreshToken) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Date date = new Date((new Date()).getTime() + jwtExpirationMs);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userPrincipal.getUsername());
        claims.put("exp", date);
        claims.put("refresh_token", refreshToken);
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(date)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenFromUsername(String username, String refreshToken) {
        Date date = new Date((new Date()).getTime() + jwtExpirationMs);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("exp", date);
        claims.put("refresh_token", refreshToken);
        return Jwts.builder()
                .setSubject(username)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(date)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        Claims claims =  Jwts.parser().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
        String username = claims.get("sub", String.class);
        return username;
    }

    public String getRefreshTokenFromJwt(String token) {
        Claims claims =  Jwts.parser().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
        String refreshToken = claims.get("refresh_token", String.class);
        return refreshToken;
    }

    public ResponseCookie generateJwtCookie(String jwt) {
        return ResponseCookie.from(accessToken, jwt).path("/api").maxAge(24 * 60 * 60).httpOnly(true).build();
    }

    public ResponseCookie generateRefreshTokenCookie(String jwt) {
        return ResponseCookie.from(refreshToken, jwt).path("/api").httpOnly(true).build();
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(accessToken, null).path("/api").build();
    }

    public ResponseCookie getCleanRefreshTokenCookie() {
        return ResponseCookie.from(refreshToken, null).path("/api").build();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
