package com.urbaniak.evacuation_support_system.integration;

import com.urbaniak.evacuation_support_system.TestcontainersConfiguration;
import com.urbaniak.evacuation_support_system.dto.Request.LogoutRequest;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "evacuation.qr-points-file=classpath:test_rooms.csv"
)
@AutoConfigureTestRestTemplate
class LocalizationIntegrationTest {
    private static final String LOCATION_URL = "/api/user/me/update-location";
    private static final String LOGOUT_URL = "/api/user/me/log-out";
    private static final String VALID_QR = "ess:v1:0001:101";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    // metody pomocnicze

    private RegisteredResponse register() {
        ResponseEntity<RegisteredResponse> response =
                restTemplate.postForEntity("/api/auth/register", null, RegisteredResponse.class);
        return response.getBody();
    }


    private HttpHeaders bearer(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private ResponseEntity<String> updateLocation(String token, String qr, Instant time){
        UpdateLocalizationRequest body = UpdateLocalizationRequest.builder()
                .qrCode(qr)
                .updateDateTime(time)
                .build();
        return restTemplate.exchange(LOCATION_URL, HttpMethod.PATCH, new HttpEntity<>(body, bearer(token)), String.class);
    }

    private ResponseEntity<String> logout(String token, Instant time){
        LogoutRequest body = LogoutRequest.builder()
                .logoutTime(time)
                .build();
        return restTemplate.exchange(LOGOUT_URL, HttpMethod.POST, new HttpEntity<>(body, bearer(token)), String.class);
    }

    private UserModel userInDatabase(RegisteredResponse user){
        return userRepository.findByUuid(user.getUuid()).orElseThrow();
    }

    private static Instant micros(Instant time){
        return time.truncatedTo(ChronoUnit.MICROS);
    }

    // testy

    @Test
    void shouldClearLocation(){
        RegisteredResponse user = register();
        Instant scanTime = micros(Instant.now().minus(Duration.ofMinutes(5)));
        Instant logoutTime = scanTime.plus(Duration.ofMinutes(2));

        assertThat(updateLocation(user.getToken(), VALID_QR, scanTime).getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<String> response = logout(user.getToken(), logoutTime);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserModel savedUser = userInDatabase(user);

        assertThat(savedUser.getRoomId()).isNull();
        assertThat(savedUser.getLocalizationUpdatedAt()).isEqualTo(logoutTime);


    }

    @Test
    void shouldRejectlogoutOlderThanLocationUpdate(){
        Instant updateTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Instant logoutTime = updateTime.minus(Duration.ofMinutes(5));


        RegisteredResponse user = restTemplate.postForEntity("api/auth/register", null, RegisteredResponse.class).getBody();
        ResponseEntity<String> updateResponse = updateLocation(user.getToken(), "ess:v1:0001:101", updateTime);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(logout(user.getToken(), logoutTime).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        UserModel savedUser = userRepository.findByUuid(user.getUuid()).orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użtkownika"));
        assertThat(savedUser.getRoomId()).isEqualTo("101");
        assertThat(savedUser.getLocalizationUpdatedAt()).isEqualTo(updateTime);


    }


}
