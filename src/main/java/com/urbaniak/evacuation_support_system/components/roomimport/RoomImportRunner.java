package com.urbaniak.evacuation_support_system.components.roomimport;

import com.urbaniak.evacuation_support_system.services.RoomImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomImportRunner implements CommandLineRunner {
    private final RoomImportService roomImportService;
    private final ResourceLoader resourceLoader;
    @Value("${evacuation.rooms-file}") private String roomsFile;

    public void run(String... args)throws Exception{
        roomImportService.importRooms(resourceLoader.getResource(roomsFile));
    }

}
