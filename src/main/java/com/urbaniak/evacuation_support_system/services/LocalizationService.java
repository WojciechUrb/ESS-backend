package com.urbaniak.evacuation_support_system.services;

import com.urbaniak.evacuation_support_system.dto.Request.LogoutRequest;
import com.urbaniak.evacuation_support_system.dto.Request.UpdateLocalizationRequest;
import com.urbaniak.evacuation_support_system.dto.Response.SimpleResponse;
import com.urbaniak.evacuation_support_system.models.user.UserModel;
import com.urbaniak.evacuation_support_system.components.building.BuildingData;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalizationService {
    private final UserRepository userRepository;
    private final BuildingData buildingData;
    private final Clock clock;

    @Transactional
    public SimpleResponse updateLocalization(UUID callerUuid, UpdateLocalizationRequest request){
        UserModel user = userRepository.findByUuid(callerUuid).orElseThrow(
                () -> new IllegalStateException("Uwierzytelniony użytkownik nie istnieje w bazie: " + callerUuid)
        );

        validateTimeUpdate(user.getLocalizationUpdatedAt(), request.getUpdateDateTime());

        Map<String, String> loc = parseQrCode(request.getQrCode());

        String roomId = loc.get("roomId");

        if(!buildingData.qrPointExists(roomId)){
            throw new IllegalArgumentException("Punkt QR nie istnieje: " + roomId);
        }

        user.setRoomId(roomId);
        user.setLocalizationUpdatedAt(request.getUpdateDateTime());
        userRepository.save(user);

        return SimpleResponse.builder()
                .message("Zaktualizowano").build();

    }

    @Transactional
    public SimpleResponse logout(UUID callerUuid, LogoutRequest request){
        UserModel user = userRepository.findByUuid(callerUuid).orElseThrow(
                () -> new IllegalStateException("Uwierzytelniony użytkownik nie istnieje w bazie: " + callerUuid)
        );

        Instant storedTime = user.getLocalizationUpdatedAt();

        validateTimeUpdate(storedTime, request.getLogoutTime());

        user.setRoomId(null);
        user.setLocalizationUpdatedAt(request.getLogoutTime());
        userRepository.save(user);
        return new SimpleResponse("Wylogowano");

    }


    // metody pomocnicze

    private Map<String, String> parseQrCode(String qr){
        if(qr == null) throw new IllegalArgumentException("qr jest null");

        String[] parts = qr.strip().replace("\uFEFF", "").split(":", -1);

        if(parts.length != 4) throw new IllegalArgumentException("Nieprawidłowy QR");

        Map<String, String> response = new HashMap<>();
        response.put("roomId", parts[3].trim().toLowerCase(Locale.ROOT));
        return response;

    }

    private void validateTimeUpdate(Instant storedTime, Instant requestTime){
        if(requestTime.isAfter(clock.instant().plus(Duration.ofMinutes(2)))){
            throw new IllegalArgumentException("updateTime z przyszłości");
        }

        if (storedTime != null && storedTime.isAfter(requestTime)) {
            throw new IllegalArgumentException("updateDateTime jest niepoprawny (wcześniejszy niż aktualna wartość w bazie)");
        }
    }
}
