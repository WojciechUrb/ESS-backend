package com.urbaniak.evacuation_support_system.components.building;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Room;
import com.urbaniak.evacuation_support_system.dto.Response.BuildingMapResponse;
import com.urbaniak.evacuation_support_system.models.qr.QrPoint;
import com.urbaniak.evacuation_support_system.models.qr.QrPointType;
import com.urbaniak.evacuation_support_system.services.MapLoader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildingData {
    private final MapLoader mapLoader;
    private final ResourceLoader resourceLoader;

    @Value("${evacuation.building-id}") private String buildingId;
    @Value("${evacuation.qr-points-file}") private String qrPointsFile;

    private Set<String> maps;
    private final Map<Integer, List<String>> floors = new TreeMap<>();
    private final Map<String, QrPoint> qrPoints = new LinkedHashMap<>();

    @PostConstruct
    void load() throws IOException {
        maps = mapLoader.loadMaps();
        loadFloors();
        loadQrPoints();
        log.info("Wczytano dane budynku {}: pięter {}, punktów QR {}", buildingId, floors.size(), qrPoints.size());
    }

    public Set<String> getMaps() {
        return maps;
    }

    public boolean qrPointExists(String id) {
        return qrPoints.containsKey(id);
    }

    public List<Room> getSolverRooms() {
        List<Room> rooms = new ArrayList<>();
        for (QrPoint point : qrPoints.values()) {
            rooms.add(point.toSolverRoom());
        }
        return rooms;
    }

    public BuildingMapResponse getBuildingMap() {
        return new BuildingMapResponse(buildingId, floors, new ArrayList<>(qrPoints.values()));
    }

    private void loadFloors() {
        for (String map : maps) {
            String[] lines = map.split("\\R");
            int floor = Integer.parseInt(lines[1].trim());

            List<String> rows = new ArrayList<>();
            for (int y = 2; y < lines.length; y++) {
                rows.add(lines[y]);
            }
            floors.put(floor, rows);
        }
    }

    private void loadQrPoints() throws IOException {
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resourceLoader.getResource(qrPointsFile).getInputStream(), StandardCharsets.UTF_8))) {
            lines = reader.lines().toList();
        }

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;

            String[] tab = line.split(";", -1);
            if (tab.length < 6) {
                throw new IllegalStateException("Nieprawidłowy wiersz " + (i + 1) + " w pliku punktów QR: " + line);
            }

            QrPointType type;
            try {
                type = QrPointType.valueOf(tab[1].trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Nieznany typ punktu w wierszu " + (i + 1) + ": " + tab[1]);
            }

            String id = tab[0].trim().toLowerCase(Locale.ROOT);
            if (qrPoints.containsKey(id)) {
                throw new IllegalStateException("Zdublowany punkt QR w wierszu " + (i + 1) + ": " + id);
            }

            qrPoints.put(id, new QrPoint(
                    id,
                    type,
                    Integer.parseInt(tab[2].trim()),
                    Integer.parseInt(tab[3].trim()),
                    Integer.parseInt(tab[4].trim()),
                    tab[5].isBlank() ? null : tab[5].trim()
            ));
        }
    }

    public List<QrPoint> getQrPoints(){
        return new ArrayList<>(qrPoints.values());
    }


}
