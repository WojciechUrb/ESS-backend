package com.urbaniak.evacuation_support_system.controllers;

import com.urbaniak.evacuation_support_system.dto.Request.LogoutRequest;
import com.urbaniak.evacuation_support_system.dto.Request.UpdateFcmTokenRequest;
import com.urbaniak.evacuation_support_system.dto.Request.UpdateLocalizationRequest;
import com.urbaniak.evacuation_support_system.dto.Response.MeResponse;
import com.urbaniak.evacuation_support_system.dto.Response.SimpleResponse;
import com.urbaniak.evacuation_support_system.security.UserPrincipal;
import com.urbaniak.evacuation_support_system.services.LocalizationService;
import com.urbaniak.evacuation_support_system.services.UserService;
import com.urbaniak.evacuation_support_system.services.evacuation_service.EvacuationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    final private LocalizationService localizationService;
    final private UserService userService;
    final private EvacuationService evacuationService;

    @GetMapping(value = "/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.status(HttpStatus.OK).body(new MeResponse(principal.getUuid(), principal.getRole(), principal.getEvacuationStatus(), principal.getRoomId()));
    }

    @PatchMapping(value = "/me/update-location")
    public ResponseEntity<SimpleResponse> updateLocalization(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateLocalizationRequest request
    ){
        return ResponseEntity.status(HttpStatus.OK).body(localizationService.updateLocalization(principal.getUuid(), request));
    }

    @PatchMapping(value = "/me/arrived")
    public ResponseEntity<SimpleResponse> markArrived(@AuthenticationPrincipal UserPrincipal principal){
        evacuationService.markArrived(principal.getUuid());
        return ResponseEntity.ok(new SimpleResponse("Zgłoszono dotarcie do punktu ewakuacji"));
    }

    @PostMapping(value = "/me/log-out")
    public ResponseEntity<SimpleResponse> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody LogoutRequest request
            ){
        return ResponseEntity.ok().body(localizationService.logout(principal.getUuid(), request));
    }

    @PostMapping("/me/update-fcm-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SimpleResponse> updateFcmToken(
            @Valid @RequestBody UpdateFcmTokenRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ){
        userService.updateFcmToken(user.getUuid(), request.getFcmToken());
        return ResponseEntity.ok(new SimpleResponse("Token updated"));
    }


}
