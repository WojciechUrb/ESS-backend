package com.urbaniak.evacuation_support_system.unit;

import com.urbaniak.evacuation_support_system.dto.Response.RegisteredResponse;
import com.urbaniak.evacuation_support_system.models.user.UserEvacuationStatus;
import com.urbaniak.evacuation_support_system.models.user.Role;
import com.urbaniak.evacuation_support_system.models.user.UserModel;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import com.urbaniak.evacuation_support_system.security.JWTService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private com.urbaniak.evacuation_support_system.services.AuthenticationService authenticationService;

    @Test
    void register_setsDefaultsAndReturnsMatchingResponse() {
        RegisteredResponse response = authenticationService.register();

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(captor.capture());
        UserModel saved = captor.getValue();

        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.getUserEvacuationStatus()).isEqualTo(UserEvacuationStatus.IDLE);
        assertThat(saved.getRoomId()).isNull();
        assertThat(saved.getLocalizationUpdatedAt()).isNull();
        assertThat(saved.getUuid()).isNotNull();

        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.getUuid()).isEqualTo(saved.getUuid());
    }
}
