package com.example.userservice.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private List<String> errors;
    private String path;

    public void addError(String message){
        if(Objects.isNull(errors)){
            errors = new ArrayList<>();
        }
        errors.add(message);
    }
}