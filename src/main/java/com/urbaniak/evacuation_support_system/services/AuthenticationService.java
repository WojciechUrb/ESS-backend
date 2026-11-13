package com.urbaniak.evacuation_support_system.services;

import com.urbaniak.evacuation_support_system.dto.Response.RegisteredResponse;
import com.urbaniak.evacuation_support_system.models.user.EvacuationStatus;
import com.urbaniak.evacuation_support_system.models.user.Role;
import com.urbaniak.evacuation_support_system.models.user.UserModel;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import com.urbaniak.evacuation_support_system.security.JWTService;
import com.urbaniak.evacuation_support_system.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JWTService jwtService;

    @Transactional
    public RegisteredResponse register(){
        UserModel user = UserModel.builder()
                .uuid(UUID.randomUUID())
                .role(Role.USER)
                .evacuationStatus(EvacuationStatus.IDLE)
                .roomId(null)
                .localizationUpdatedAt(null)
                .build();

        userRepository.save(user);
        return new RegisteredResponse(user.getUuid(), user.getRole(), jwtService.generateToken(new UserPrincipal(user)));
    }


}
