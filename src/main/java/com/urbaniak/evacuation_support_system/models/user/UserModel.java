package com.urbaniak.evacuation_support_system.models.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "room_id", nullable = true)
    private String roomId;

    @Column(name = "localization_updated_at", nullable = true)
    private Instant localizationUpdatedAt;

    @Column(name = "evacuation_status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private UserEvacuationStatus userEvacuationStatus;

    @Column(name = "push_token", length = 512)
    private String fcmToken;

}
