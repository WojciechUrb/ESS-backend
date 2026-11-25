package com.urbaniak.evacuation_support_system.integration;

import com.urbaniak.evacuation_support_system.TestcontainersConfiguration;
import com.urbaniak.evacuation_support_system.dto.Request.UpdateLocalizationRequest;
import com.urbaniak.evacuation_support_system.dto.Response.RegisteredResponse;
import com.urbaniak.evacuation_support_system.models.user.UserModel;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "evacuation.qr-points-file=classpath:test_rooms.csv")
@AutoConfigureTestRestTemplate
public class LocalizationUpdateIntegrationTest {

    private static final String URL = "/api/user/me/update-location";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private ResponseEntity<String> updateLocation(String token, String qr, Instant time){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        UpdateLocalizationRequest body = UpdateLocalizationRequest.builder()
                .qrCode(qr)
                .updateDateTime(time)
                .build();
        return restTemplate.exchange(URL, HttpMethod.PATCH, new HttpEntity<>(body, headers), String.class);

    }

    @Test
    void shouldUpdateUserLocation(){
        RegisteredResponse user = restTemplate.postForEntity("api/auth/register", null, RegisteredResponse.class).getBody();
        Instant time = Instant.now().truncatedTo(ChronoUnit.MICROS);

        ResponseEntity<String> response = updateLocation(user.getToken(), "ess:v1:0001:101", time);

        assertThat(response.getStatusCode().isSameCodeAs(HttpStatus.OK));
        UserModel savedUser = userRepository.findByUuid(user.getUuid()).orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użtkownika"));
        assertThat(savedUser.getRoomId()).isEqualTo("101");
        assertThat(savedUser.getLocalizationUpdatedAt()).isEqualTo(time);
    }

    @Test
    void shouldRejestUnknownRoomAndKeepLocationEmpty(){
        RegisteredResponse user = restTemplate.postForEntity("api/auth/register", null, RegisteredResponse.class).getBody();

        ResponseEntity<String> responseEntity = updateLocation(user.getToken(), "ess:v1:0001:9999", Instant.now());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        UserModel savedUser = userRepository.findByUuid(user.getUuid()).orElseThrow();
        assertThat(savedUser.getRoomId()).isNull();
        assertThat(savedUser.getLocalizationUpdatedAt()).isNull();
    }


}
