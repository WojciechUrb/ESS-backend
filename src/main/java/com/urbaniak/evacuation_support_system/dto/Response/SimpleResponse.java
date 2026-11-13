package com.urbaniak.evacuation_support_system.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SimpleResponse{
    private String message;
}
