package com.urbaniak.evacuation_support_system.dto.Response;

import com.urbaniak.evacuation_support_system.models.qr.QrPoint;

import java.util.List;
import java.util.Map;

public record BuildingMapResponse(String buildingId, Map<Integer, List<String>> floors, List<QrPoint> qrPoints) {
}
