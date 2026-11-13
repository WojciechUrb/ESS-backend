package com.urbaniak.evacuation_support_system.controllers;

import com.urbaniak.evacuation_support_system.dto.Response.RegisteredResponse;
import com.urbaniak.evacuation_support_system.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<RegisteredResponse> register (){
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register());
    }

}
