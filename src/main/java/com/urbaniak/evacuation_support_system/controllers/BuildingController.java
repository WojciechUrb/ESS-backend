package com.urbaniak.evacuation_support_system.controllers;

import com.urbaniak.evacuation_support_system.components.building.BuildingData;
import com.urbaniak.evacuation_support_system.dto.Response.BuildingMapResponse;
import com.urbaniak.evacuation_support_system.dto.Response.BuildingOccupancyResponse;
import com.urbaniak.evacuation_support_system.services.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/building")
@RequiredArgsConstructor
public class BuildingController {
    private final BuildingData buildingData;
    private final BuildingService buildingService;

    @GetMapping("/map")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BuildingMapResponse> getMap() {
        return ResponseEntity.ok(buildingData.getBuildingMap());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/building-occupancy")
    public ResponseEntity<BuildingOccupancyResponse> getBuildingOccupancy(){ return ResponseEntity.ok(buildingService.getOccupancy()); }

}
