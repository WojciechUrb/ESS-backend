package com.urbaniak.evacuation_support_system.dto.Response;

import com.urbaniak.evacuation_support_system.models.user.UserEvacuationStatus;
import com.urbaniak.evacuation_support_system.models.user.Role;

import java.util.UUID;

public record MeResponse(UUID uuid, Role role, UserEvacuationStatus userEvacuationStatus, String roomId) {
}
