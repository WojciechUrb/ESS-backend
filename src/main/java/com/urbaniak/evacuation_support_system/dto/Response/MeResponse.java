package com.urbaniak.evacuation_support_system.dto.Response;

import com.urbaniak.evacuation_support_system.models.user.EvacuationStatus;
import com.urbaniak.evacuation_support_system.models.user.Role;
import lombok.Builder;

import java.util.UUID;

public record MeResponse(UUID uuid, Role role, EvacuationStatus evacuationStatus, String roomId) {
}
