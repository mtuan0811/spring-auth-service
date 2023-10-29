package com.example.userservice.security.jwt;

import java.util.Objects;
import java.util.Optional;

import com.example.userservice.components.MutableHttpServletRequest;
import com.example.userservice.domain.models.Session;
import com.example.userservice.repositories.SessionUserRepository;
import com.example.userservice.security.services.impl.UserDetailsImpl;
import com.example.userservice.security.services.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.WebUtils;


public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    SessionUserRepository sessionUserRepository;

    @Autowired
    HandlerExceptionResolver handlerExceptionResolver;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain){
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
        try{
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String refreshToken = jwtUtils.getRefreshTokenFromJwt(jwt);
                Optional<Session> session = sessionUserRepository.findSessionByRefreshToken(refreshToken);
                if (session.isPresent()) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                            userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(mutableRequest));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    mutableRequest.putHeader("X-User-Id", userDetails.getId());
                    mutableRequest.putHeader("X-User-Role", userDetails.getAuthorities().toString());
                }else{
                    SecurityContextHolder.getContext().setAuthentication(null);
                }
            }
            filterChain.doFilter(mutableRequest, response);
        }catch (Exception e){
            logger.error("[Filter] UnAuthentication: {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private String parseJwt(HttpServletRequest request) {
        Cookie headerTokenCookie = WebUtils.getCookie(request, "access_token");
        if(!Objects.isNull(headerTokenCookie) && !headerTokenCookie.getValue().isEmpty()){
            return WebUtils.getCookie(request, "access_token").getValue();
        }
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }
        return null;
    }
}