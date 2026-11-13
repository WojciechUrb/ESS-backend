package com.urbaniak.evacuation_support_system.integration;

import com.urbaniak.evacuation_support_system.TestcontainersConfiguration;
import com.urbaniak.evacuation_support_system.dto.Response.RegisteredResponse;
import com.urbaniak.evacuation_support_system.models.user.EvacuationStatus;
import com.urbaniak.evacuation_support_system.models.user.Role;
import com.urbaniak.evacuation_support_system.models.user.UserModel;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AuthenticationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterUserWithDefaults() {
        ResponseEntity<RegisteredResponse> response =
                restTemplate.postForEntity("/api/auth/register", null, RegisteredResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        RegisteredResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getRole()).isEqualTo(Role.USER);
        assertThat(body.getUuid()).isNotNull();

        UserModel saved = userRepository.findByUuid(body.getUuid()).orElseThrow();
        assertThat(saved.getEvacuationStatus()).isEqualTo(EvacuationStatus.IDLE);
        assertThat(saved.getRoomId()).isNull();
        assertThat(saved.getLocalizationUpdatedAt()).isNotNull();
    }

    @Test
    public void shouldBlockAccessToEndpointAndReturn401(){
        ResponseEntity<String> response = restTemplate.getForEntity("api/endpoint", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)).isTrue();
    }

    @Test
    void shouldAllowAccessToProtectedEndpointWithValidToken() {
        ResponseEntity<RegisteredResponse> registerResponse =
                restTemplate.postForEntity("/api/auth/register", null, RegisteredResponse.class);
        String token = registerResponse.getBody().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<String> response = restTemplate.exchange(
                "/actuator/health", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
