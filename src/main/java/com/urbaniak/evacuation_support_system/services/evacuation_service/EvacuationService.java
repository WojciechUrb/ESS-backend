package com.urbaniak.evacuation_support_system.services.evacuation_service;

import com.urbaniak.evacuation_support_system.components.building.BuildingData;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.EvacuationSolver;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Cell;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Room;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.UserLocation;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.output.dto.MobileRoute;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.output.dto.OutputData;
import com.urbaniak.evacuation_support_system.dto.BlockedCell;
import com.urbaniak.evacuation_support_system.dto.Request.BlockedCellsRequest;
import com.urbaniak.evacuation_support_system.dto.Response.RouteResponse;
import com.urbaniak.evacuation_support_system.models.user.UserEvacuationStatus;
import com.urbaniak.evacuation_support_system.models.user.UserModel;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import com.urbaniak.evacuation_support_system.services.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvacuationService {
    private final AtomicReference<State> state = new AtomicReference<>(new State(SystemEvacuationState.IDLE, null));

    private final UserRepository userRepository;
    private final BuildingData buildingData;
    private final PushNotificationService pushNotificationService;
    private final EvacuationSolver evacuationSolver;

    private  Set<Cell> blockedCells = new HashSet<>();



    public void startCalculating(){
        final State currentState = state.get();
        switch (currentState.systemEvacuationState()) {
            case CALCULATING -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Obliczenia uruchomione"
            );
            case ACTIVE -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ewakuacja w toku"
            );
            default -> {  }
        }

        final State calculatingState = new State(SystemEvacuationState.CALCULATING, null);

        if (!state.compareAndSet(currentState, calculatingState)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stan uległ zmianie"
            );
        }
        blockedCells.clear();

        final OutputData outputData;
        try {
            List<UserLocation> userLocationList = getLocationsAndActivateUsersEvacuationStatuses();
            pushNotificationService.sendNotificationEvacuationStarted();

            Set<String> maps = buildingData.getMaps();
            List<Room> rooms = buildingData.getSolverRooms();

            outputData = evacuationSolver.solve(maps, rooms, userLocationList, new HashSet<>(blockedCells));
        } catch (Exception e) {
            log.error("Wyznaczanie tras zakończone niepowodzeniem", e);

            // Wracamy do IDLE tylko jeśli nikt w międzyczasie nie zmienił stanu
            if (state.compareAndSet(calculatingState, new State(SystemEvacuationState.IDLE, null))) {
                pushNotificationService.sendNotificationIndependentEvacuation(userRepository.findTokensByEvacuationStatus(UserEvacuationStatus.ACTIVE));
                resetUsersEvacuationStatuses();
            }
            return;
        }

        if (state.compareAndSet(calculatingState, new State(SystemEvacuationState.ACTIVE, outputData))) {
            pushNotificationService.sendNotificationRoutesReady();
        } else {
            log.info("Przebieg obliczeń anulowany w trakcie - wynik odrzucony");
        }
    }

    public void stopEvacuation(){
        final State prev = state.getAndSet(new State(SystemEvacuationState.IDLE, null));
        if(prev.systemEvacuationState() == SystemEvacuationState.IDLE){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "EWAKUACJA NIEAKTYWNA"
            );
        }

        List<String> tokens = resetUsersEvacuationStatuses();
        pushNotificationService.sendNotificationEvacuationCancelled(tokens);

    }

    public synchronized void updateBlockedCellsAndRecalculate(BlockedCellsRequest request){
        final State currentState = state.get();
        if(!currentState.systemEvacuationState().equals(SystemEvacuationState.ACTIVE)){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "EWAKUACJA NIEAKTYWNA"
            );
        }
        if(request.blockedCells() == null || request.blockedCells().isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "PUSTY REQUEST"
            );
        }
        for(BlockedCell blockedCell : request.blockedCells()) blockedCells.add(new Cell(blockedCell.x(), blockedCell.y(), blockedCell.floor()));

        List<UserLocation> locations = new ArrayList<>();
        Set<UUID> activeIds = new HashSet<>();
        for(UserModel user : userRepository.findAllByEvacuationStatus(UserEvacuationStatus.ACTIVE)){
            locations.add(new UserLocation(user.getUuid(), user.getRoomId()));
            activeIds.add(user.getUuid());
        }
        if(locations.isEmpty()) return;

        // rekaltulacja
        OutputData outputData;
        try {
            Set<String> maps = buildingData.getMaps();
            List<Room> rooms = buildingData.getSolverRooms();

            outputData = evacuationSolver.solve(maps, rooms, locations, new HashSet<>(blockedCells));
        } catch (Exception e) {
            log.error("Wyznaczanie tras zakończone niepowodzeniem", e);
            if (state.compareAndSet(currentState, new State(SystemEvacuationState.ACTIVE, null))) {
                pushNotificationService.sendNotificationIndependentEvacuation(userRepository.findTokensByEvacuationStatus(UserEvacuationStatus.ACTIVE));
            }
            return;
        }

        if (!state.compareAndSet(currentState, new State(SystemEvacuationState.ACTIVE, outputData))) {
            log.info("Ewakuacja zakończona w trakcie przeliczania - wynik odrzucony");
            return;
        }

        Map<UUID, MobileRoute> oldRoutes = new HashMap<>();
        Map<UUID, MobileRoute> newRoutes = outputData.routes();
        Set<UUID> changedRoute = new HashSet<>();
        Set<UUID> lostRoute = new HashSet<>();

        if(currentState.outputData() != null) oldRoutes = currentState.outputData().routes();

        for(UUID uuid : newRoutes.keySet()){
            if(!newRoutes.get(uuid).equals(oldRoutes.get(uuid))) changedRoute.add(uuid);
        }

        for(UUID uuid : oldRoutes.keySet()){
            if(!newRoutes.containsKey(uuid) && activeIds.contains(uuid)) lostRoute.add(uuid);
        }

        if(!changedRoute.isEmpty()) pushNotificationService.sendNotificationNewRoutes(userRepository.findTokensByUuids(changedRoute));
        if(!lostRoute.isEmpty()) pushNotificationService.sendNotificationIndependentEvacuation(userRepository.findTokensByUuids(lostRoute));
    }

    public RouteResponse getRoute(UUID userId){
        final State currentState = state.get();
        final RouteResponse response;

        switch (currentState.systemEvacuationState()) {
            case ACTIVE -> {
                if(currentState.outputData() == null){
                    response = RouteResponse.noRoute();
                }else{
                    MobileRoute route = currentState.outputData().routes().get(userId);
                    if(route != null){
                        response = RouteResponse.ready(route);
                    }else{
                        response = RouteResponse.noRoute();
                    }
                }
            }
            case CALCULATING -> response = RouteResponse.calculating();
            case IDLE -> response = RouteResponse.noEvacuation();
            default -> throw new IllegalStateException("Unexpected state: " + currentState.systemEvacuationState());
        }

        return response;
    }



    @Transactional
    public void markArrived(UUID uuid){
        UserModel user = userRepository.findByUuid(uuid).orElseThrow(
                () -> new IllegalStateException("Uwierzytelniony użytkownik nie istnieje w bazie: " + uuid)
        );

        if (user.getUserEvacuationStatus() != UserEvacuationStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Użytkownik nie uczestniczy w ewakuacji");
        }

        user.setUserEvacuationStatus(UserEvacuationStatus.ARRIVED);
        userRepository.save(user);
    }


    public List<UserLocation> getLocationsAndActivateUsersEvacuationStatuses(){
        List<UserModel> users = userRepository.findAllLocations(Instant.now().minus(Duration.ofHours(24)));
        List<UserLocation> locations = new ArrayList<>();

        for(UserModel user : users){
            user.setUserEvacuationStatus(UserEvacuationStatus.ACTIVE);
            locations.add(new UserLocation(user.getUuid(), user.getRoomId()));
        }
        userRepository.saveAll(users);
        return locations;
    }

    public List<String> resetUsersEvacuationStatuses(){
        List<UserModel> users = userRepository.findAll();
        List<String> tokens = new ArrayList<>();

        for (UserModel user : users) {
            if (user.getUserEvacuationStatus() != UserEvacuationStatus.IDLE) {
                user.setUserEvacuationStatus(UserEvacuationStatus.IDLE);

                if (user.getFcmToken() != null) {
                    tokens.add(user.getFcmToken());
                }
            }
        }
        userRepository.saveAll(users);
        return tokens;
    }

}
