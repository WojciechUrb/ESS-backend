package com.urbaniak.evacuation_support_system.integration;

import com.urbaniak.evacuation_support_system.models.room.RoomModel;
import com.urbaniak.evacuation_support_system.repositories.RoomRepository;
import com.urbaniak.evacuation_support_system.services.RoomImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import com.urbaniak.evacuation_support_system.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "evacuation.rooms-file=classpath:test_rooms.csv")
public class RoomImportIntegrationTest {

    private static final String HEADER = "building_id;room_id;floor;door_x;door_y;label";

    @Autowired private RoomImportService roomImportService;
    @Autowired private RoomRepository roomRepository;

    @BeforeEach
    void cleanDb(){
        roomRepository.deleteAll();
    }

    private Resource csv(String... rows){
        String content = HEADER + "\n" + String.join("\n", rows);
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
    }

    private RoomModel find(String buildingId, String roomId){
        return roomRepository.findByRoomId(roomId).orElseThrow();
    }

    @Test
    void shouldImportRooms(){
        roomImportService.importRooms(csv(
                "0001;101;1;3;1;sekratariat",
                       "0001;123A;1;5;0;"
        ));

        assertThat(roomRepository.count()).isEqualTo(2);

        RoomModel r1 = find("0001", "101");
        assertThat(r1.getFloor()).isEqualTo(1);
        assertThat(r1.getX()).isEqualTo(3);
        assertThat(r1.getY()).isEqualTo(1);
        assertThat(r1.getLabel()).isEqualTo("sekratariat");

        RoomModel r2 = find("0001", "123a");
        assertThat(r2.getLabel()).isNull();


    }

    @Test
    void shouldBeTheSameAfterImportedTwice(){
        Resource file = csv(
                "0001;101;1;3;1;sekratariat",
                "0001;123A;1;5;0;"
        );

        roomImportService.importRooms(file);
        int firstId = find("0001", "101").getId();

        roomImportService.importRooms(file);
        assertThat(roomRepository.count()).isEqualTo(2);
        assertThat(find("0001", "101").getId()).isEqualTo(firstId);

    }

    @Test
    void shouldRollBackWholeImportWhenFileContainsDuplicate(){
        roomImportService.importRooms(csv("0001;101;1;3;1;stary"));

        assertThatThrownBy(() -> roomImportService.importRooms(csv(
                "0001;200;0;1;1;Nowy",
                "0001;300;0;2;1;Duplikat",
                "0001;300;0;2;1;Duplikat"
        ))).isInstanceOf(DataIntegrityViolationException.class);

        assertThat(roomRepository.count()).isEqualTo(1);
        assertThat(find("0001", "101").getLabel()).isEqualTo("stary");
    }

}
