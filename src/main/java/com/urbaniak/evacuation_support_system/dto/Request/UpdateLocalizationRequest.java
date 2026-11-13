package com.urbaniak.evacuation_support_system.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLocalizationRequest {
    @NotBlank(message = "QrCode jest wymagany!")
    @Size(max = 128)
    String qrCode;

    @NotNull(message = "updateDateTime jest wymagane")
    Instant updateDateTime;
}