package com.example.userservice.domain.payload.response;

import com.example.userservice.domain.dto.ErrorDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseResponse<T> {
    private T data;
    private ErrorDto error;
}
