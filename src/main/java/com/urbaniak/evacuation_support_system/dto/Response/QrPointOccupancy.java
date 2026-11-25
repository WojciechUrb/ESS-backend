package com.urbaniak.evacuation_support_system.dto.Response;

import com.urbaniak.evacuation_support_system.models.qr.QrPointType;
import lombok.Builder;

@Builder
public record QrPointOccupancy(
        String qrId,
        QrPointType type,
        int floor,
        int x,
        int y,
        String label,
        int occupancy
) {
}
