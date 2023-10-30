package com.example.userservice.domain.models;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@EqualsAndHashCode()
@Document(collection = "refreshToken")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndexes({
        @CompoundIndex(name = "userId_refreshtoken", def = "{'userId' : 1, 'user.id': 1}")
})
public class RefreshToken extends BaseEntity{

    @NotBlank
    private String token;

    @NotBlank
    private Date expiryDate;

    @NotBlank
    private String userId;
}
