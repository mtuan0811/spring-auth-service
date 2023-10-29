package com.example.userservice.components;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders;

    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
        this.customHeaders = new LinkedCaseInsensitiveMap<>();
    }

    public void putHeader(String name, String value) {
        this.customHeaders.put(name, value);
    }

    private HttpServletRequest getServletRequest() {
        return (HttpServletRequest) getRequest();
    }

    @Override
    public String getHeader(String name) {
        return Optional.ofNullable(customHeaders.get(name))
                .orElseGet(() -> getServletRequest().getHeader(name));
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Optional.ofNullable(customHeaders.get(name))
                .map(v -> Collections.enumeration(List.of(v)))
                .orElseGet(() -> getServletRequest().getHeaders(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(
                Stream.concat(
                                customHeaders.keySet().stream(),
                                StreamSupport.stream(
                                        Spliterators.spliteratorUnknownSize(
                                                getServletRequest()
                                                        .getHeaderNames()
                                                        .asIterator(),
                                                Spliterator.ORDERED), false))
                        .collect(Collectors.toSet()));
    }
}