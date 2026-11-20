package com.urbaniak.evacuation_support_system.controllers;

import com.urbaniak.evacuation_support_system.dto.Request.BlockedCellsRequest;
import com.urbaniak.evacuation_support_system.dto.Response.RouteResponse;
import com.urbaniak.evacuation_support_system.dto.Response.SimpleResponse;
import com.urbaniak.evacuation_support_system.security.UserPrincipal;
import com.urbaniak.evacuation_support_system.services.evacuation_service.EvacuationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evacuation")
@RequiredArgsConstructor
public class EvacuationController {
    private final EvacuationService evacuationService;

    @PostMapping("/start-evacuation")
    public ResponseEntity<SimpleResponse> startEvacuation(){
        evacuationService.startCalculating();
        return ResponseEntity.ok(new SimpleResponse("Rozpoczeto ewakuacje"));
    }

    @PostMapping("/cancel-evacuation")
    public ResponseEntity<SimpleResponse> cancelEvacuation(){
        evacuationService.stopEvacuation();
        return ResponseEntity.ok(new SimpleResponse("Zakonczono ewakuacje"));
    }

    @PostMapping("/report-blocked-corridor")
    public ResponseEntity<SimpleResponse> reportBlockedCorridor(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody BlockedCellsRequest request
            ){
        evacuationService.updateBlockedCellsAndRecalculate(request);
        return ResponseEntity.ok(new SimpleResponse("Zaktualizowano"));
    }

    @GetMapping("/get-route")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RouteResponse> getRoute(
            @AuthenticationPrincipal UserPrincipal user
    ){
        return ResponseEntity.ok(evacuationService.getRoute(user.getUuid()));
    }

}
