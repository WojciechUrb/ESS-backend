package com.urbaniak.evacuation_support_system.services;

import com.urbaniak.evacuation_support_system.components.building.BuildingData;
import com.urbaniak.evacuation_support_system.dto.Response.BuildingOccupancyResponse;
import com.urbaniak.evacuation_support_system.dto.Response.QrPointOccupancy;
import com.urbaniak.evacuation_support_system.models.qr.QrPoint;
import com.urbaniak.evacuation_support_system.models.user.UserEvacuationStatus;
import com.urbaniak.evacuation_support_system.models.user.UserModel;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private final UserRepository userRepository;
    private final BuildingData buildingData;

    public BuildingOccupancyResponse getOccupancy(){
        List<UserModel> users = userRepository.findAllLocations(Instant.now().minus(Duration.ofHours(24)));
        List<QrPoint> qrPointList = buildingData.getQrPoints();
        Map<String, Integer> qrPointsOccupancyMap = new HashMap<>();
        List<QrPointOccupancy> qrPointsOccupancyList = new ArrayList<>();

        for(QrPoint qrPoint : qrPointList) qrPointsOccupancyMap.put(qrPoint.id(), 0);


        for(UserModel user : users){
            if (user.getUserEvacuationStatus() == UserEvacuationStatus.ARRIVED) continue;

            if(qrPointsOccupancyMap.containsKey(user.getRoomId())){
                qrPointsOccupancyMap.put(user.getRoomId(), qrPointsOccupancyMap.get(user.getRoomId()) + 1);
            }
        }

        for(QrPoint point : qrPointList){
                qrPointsOccupancyList.add(
                        new QrPointOccupancy(point.id(), point.type(), point.floor(), point.x(), point.y(), point.label(), qrPointsOccupancyMap.get(point.id())));
        }

        return new BuildingOccupancyResponse(qrPointsOccupancyList);
    }
}
