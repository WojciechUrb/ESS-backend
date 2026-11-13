package com.urbaniak.evacuation_support_system.repositories;

import com.urbaniak.evacuation_support_system.models.room.RoomModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<RoomModel, Integer> {

    Optional<RoomModel> findByRoomId(String roomId);

}
