package com.urbaniak.evacuation_support_system.dto.Response;

import com.urbaniak.evacuation_support_system.models.user.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisteredResponse{
    private UUID uuid;
    @Enumerated(value = EnumType.STRING)
    private Role role;
    private String token;
}
