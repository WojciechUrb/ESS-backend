package com.urbaniak.evacuation_support_system.services;

import com.urbaniak.evacuation_support_system.models.room.RoomModel;
import com.urbaniak.evacuation_support_system.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@Service
public class RoomImportService {
    private final RoomRepository roomRepository;

    @Transactional
    public void importRooms(Resource resource) {
        List<String> lines;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))){
            lines = reader.lines().toList();
        }catch (IOException ex){
            throw new IllegalStateException("Nie można odczytać pliku pokoi: " + resource.getDescription(), ex);
        }

        Map<String, RoomModel> dbRooms = new HashMap<>();
        for(RoomModel room : roomRepository.findAll()){
            dbRooms.put(room.getRoomId(), room);
        }

        List<RoomModel> toSave = new ArrayList<>();
        for(int i = 1; i < lines.size(); i++){
            String line = lines.get(i);
            if(line.isBlank()) continue;
            String[] tab = line.split(";", -1);
            if(tab.length < 5){
                throw new IllegalStateException("Nieprawidłowy wiersz " + (i + 1) + " w pliku pokoi: " + line);
            }

            String roomId = tab[0].trim().toLowerCase(Locale.ROOT);

            RoomModel room = dbRooms.remove(roomId);
            if(room == null){
                room = RoomModel.builder()
                        .roomId(roomId)
                        .build();
            }

            room.setFloor(Integer.parseInt(tab[1].trim()));
            room.setX(Integer.parseInt(tab[2].trim()));
            room.setY(Integer.parseInt(tab[3].trim()));
            room.setLabel(tab[4].isBlank() ? null : tab[4].trim());
            toSave.add(room);

        }
        roomRepository.saveAll(toSave);
        roomRepository.deleteAll(dbRooms.values());

    }


}
