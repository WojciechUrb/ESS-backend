package com.urbaniak.evacuation_support_system.services;

import com.google.firebase.messaging.*;
import com.urbaniak.evacuation_support_system.models.user.UserEvacuationStatus;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {
    private final UserRepository userRepository;

    final String CHANNEL_ID = "evacuation_alerts";


    public void sendPushToUsers(List<String> targetFcmTokens, String buildingId, String title, String body, String type) {
        if (targetFcmTokens == null || targetFcmTokens.isEmpty()) {
            log.warn("Lista tokenów fcm jest pusta");
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setTtl(Duration.ofMinutes(50).toMillis())
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .setChannelId(CHANNEL_ID)
                        .setTag("evacuation")
                        .build())
                .build();

        int tokenCount = targetFcmTokens.size();
        List<List<String>> groups = new ArrayList<>();


        for(int i = 0; i < tokenCount; i+=500){
            groups.add(targetFcmTokens.subList(i, Math.min(i+500, tokenCount)));
        }

        for(List<String> group :  groups){
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(group)
                    .setNotification(notification)
                    .setAndroidConfig(androidConfig)
                    .putData("type", type)
                    .build();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            } catch (FirebaseMessagingException e) {
                log.error("Wysyłanie powiadomień przez Firebase zakończenie niepowodzeniem", e);
            }

        }
    }

    public void sendNotificationEvacuationStarted(){
        sendPushToUsers(userRepository.findAllTokens(Instant.now().minus(Duration.ofHours(24))),
                "0001",
                "EWAKUACJA",
                "Wkrótce otrzymasz trasę ewakuacji",
                "EVACUATION"
        );
    }

    public void sendNotificationRoutesReady(){
        sendPushToUsers(userRepository.findTokensByEvacuationStatus(UserEvacuationStatus.ACTIVE),
                "0001",
                "TRASA GOTOWA",
                "Sprawdź trasę ewakuacji z budynku!",
                "ROUTE"
        );
    }

    public void sendNotificationIndependentEvacuation(List<String> tokens){
        sendPushToUsers(tokens,
                "0001",
                "EWAKUUJ SIĘ SAMODZIELNIE",
                "Udaj się do najbliższego wyjścia z budynku!",
                "NO-ROUTE"
        );
    }

    public void sendNotificationNewRoutes(List<String> tokens){
        sendPushToUsers(tokens,
                "0001",
                "TRASA ZMIENIONA", // ZMIANA: push idzie tylko do osób ze zmienioną trasą
                "Twoja trasa ewakuacji została zmieniona - sprawdź nową!",
                "ROUTE"
        );
    }

    public void sendNotificationEvacuationCancelled(List<String> tokens){
        sendPushToUsers(tokens,
                "0001",
                "EWAKUACJA ZAKOŃCZONA",
                "Upewnij się, czy w budynku jest bezpiecznie",
                "END"
        );
    }

}
