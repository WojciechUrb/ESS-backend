package com.urbaniak.evacuation_support_system.dto.Response;

import java.util.List;

public record BuildingOccupancyResponse(
        List<QrPointOccupancy> qrPoints
) {
}
