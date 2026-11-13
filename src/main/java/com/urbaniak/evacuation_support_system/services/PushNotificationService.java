package com.urbaniak.evacuation_support_system.services;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PushNotificationService {
    final String CHANNEL_ID = "evacuation_alerts";


    public void sendPushToUsers(List<String> targetFcmTokens, String buildingId) {
        if (targetFcmTokens == null || targetFcmTokens.isEmpty()) {
            log.warn("Lista tokenów fcm jest pusta");
            return;
        }

        Notification notification = Notification.builder()
                .setTitle("ROZPOCZĘTO EWAKUACJĘ")
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
                    .putData("type", "EVACUATION")
                    .build();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send push notification via Firebase", e);
            }

        }



        }


}
