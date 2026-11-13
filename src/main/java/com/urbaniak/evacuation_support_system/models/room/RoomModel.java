package com.urbaniak.evacuation_support_system.models.room;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Length;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rooms")
public class RoomModel {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "map_door_x", nullable = false)
    private int x;

    @Column(name = "map_door_y", nullable = false)
    private int y;

    @Column(name = "floor", nullable = false)
    private int floor;

    @Column(name = "label")
    private String label;

}
