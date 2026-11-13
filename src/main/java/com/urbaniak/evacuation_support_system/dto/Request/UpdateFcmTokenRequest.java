package com.urbaniak.evacuation_support_system.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFcmTokenRequest {
    @NotBlank
    @Size(max = 512)
    private String fcmToken;
}
