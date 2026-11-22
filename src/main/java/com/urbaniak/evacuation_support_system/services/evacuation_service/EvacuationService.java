package com.urbaniak.evacuation_support_system.services.evacuation_service;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.EvacuationSolver;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Cell;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Room;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.UserLocation;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.output.dto.MobileRoute;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.output.dto.OutputData;
import com.urbaniak.evacuation_support_system.dto.Request.BlockedCellsRequest;
import com.urbaniak.evacuation_support_system.dto.Response.RouteResponse;
import com.urbaniak.evacuation_support_system.models.room.RoomModel;
import com.urbaniak.evacuation_support_system.models.user.EvacuationStatus;
import com.urbaniak.evacuation_support_system.models.user.UserModel;
import com.urbaniak.evacuation_support_system.repositories.RoomRepository;
import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import com.urbaniak.evacuation_support_system.services.MapLoader;
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
    private final AtomicReference<State> state = new AtomicReference<>(new State(EvacuationState.IDLE, null));

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PushNotificationService pushNotificationService;
    private final EvacuationSolver evacuationSolver;
    private final MapLoader mapLoader;

    private  List<Cell> blockedCells = new ArrayList<>();



    public void startCalculating(){
        final State currentState = state.get();
        switch (currentState.evacuationState()) {
            case CALCULATING -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Obliczenia uruchomione"
            );
            case ACTIVE -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ewakuacja w toku"
            );
            default -> { }
        }

        final State calculatingState = new State(EvacuationState.CALCULATING, null);
        if (!state.compareAndSet(currentState, calculatingState)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stan uległ zmianie"
            );
        }

        final OutputData outputData;
        try {
            List<UserLocation> userLocationList = getLocationsAndActivateUsersEvacuationStatuses();
            sendNotificationEvacuationStarted();

            Set<String> maps = mapLoader.loadMaps();

            List<Room> rooms = roomRepository.findAll().stream()
                    .map(RoomModel::toSolverRoom)
                    .toList();

            outputData = evacuationSolver.solve(maps, rooms, userLocationList, blockedCells);
        } catch (Exception e) {
            log.error("Wyznaczanie tras zakończone niepowodzeniem", e);

            // Wracamy do IDLE tylko jeśli nikt w międzyczasie nie zmienił stanu
            if (state.compareAndSet(calculatingState, new State(EvacuationState.IDLE, null))) {
                sendNotificationIndependentEvacuation();
                resetUsersEvacuationStatuses();
            }
            return;
        }

        // Publikujemy wynik tylko jeśli ten przebieg jest nadal aktualny
        if (state.compareAndSet(calculatingState, new State(EvacuationState.ACTIVE, outputData))) {
            sendNotificationRoutesReady();
        } else {
            log.info("Przebieg obliczeń anulowany w trakcie - wynik odrzucony");
        }
    }

    public void stopEvacuation(){
        final State prev = state.getAndSet(new State(EvacuationState.IDLE, null));
        if(prev.evacuationState() == EvacuationState.IDLE){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "EWAKUACJA NIEAKTYWNA"
            );
        }

        List<String> tokens = resetUsersEvacuationStatuses();
        sendNotificationEvacuationCancelled(tokens);

    }

    public RouteResponse getRoute(UUID userId){
        // Jeden odczyt: stan i wynik pochodzą z tej samej migawki
        final State currentState = state.get();
        return switch (currentState.evacuationState()) {
            case ACTIVE -> {
                MobileRoute route = currentState.outputData().routes().get(userId);
                yield route != null ? RouteResponse.ready(route) : RouteResponse.noRoute();
            }
            case CALCULATING -> RouteResponse.calculating();
            case IDLE -> RouteResponse.noEvacuation();
        };
    }

    public void updateBlockedCellsAndRecalculate(BlockedCellsRequest request){

    }

    @Transactional
    public void markArrived(UUID uuid){
        UserModel user = userRepository.findByUuid(uuid).orElseThrow(
                () -> new IllegalStateException("Uwierzytelniony użytkownik nie istnieje w bazie: " + uuid)
        );

        if (user.getEvacuationStatus() != EvacuationStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Użytkownik nie uczestniczy w ewakuacji");
        }

        user.setEvacuationStatus(EvacuationStatus.ARRIVED);
        userRepository.save(user);
    }


    public List<UserLocation> getLocationsAndActivateUsersEvacuationStatuses(){
        List<UserModel> users = userRepository.findAllLocations(Instant.now().minus(Duration.ofHours(24)));
        List<UserLocation> locations = new ArrayList<>();

        for(UserModel user : users){
            user.setEvacuationStatus(EvacuationStatus.ACTIVE);
            locations.add(new UserLocation(user.getUuid(), user.getRoomId()));
        }
        userRepository.saveAll(users);
        return locations;
    }

    public List<String> resetUsersEvacuationStatuses(){
        List<UserModel> users = userRepository.findAll();
        List<String> tokens = new ArrayList<>();

        for (UserModel user : users) {
            if (user.getEvacuationStatus() != EvacuationStatus.IDLE) {
                user.setEvacuationStatus(EvacuationStatus.IDLE);

                if (user.getFcmToken() != null) {
                    tokens.add(user.getFcmToken());
                }
            }
        }
        userRepository.saveAll(users);
        return tokens;
    }


    public void sendNotificationEvacuationStarted(){
        pushNotificationService.sendPushToUsers(userRepository.findAllTokens(Instant.now().minus(Duration.ofHours(24))),
                "0001",
                "EWAKUACJA",
                "Wkrótce otrzymasz trasę ewakuacji",
                "EVACUATION"
        );
    }

    public void sendNotificationRoutesReady(){
        pushNotificationService.sendPushToUsers(userRepository.findTokensByEvacuationStatus(EvacuationStatus.ACTIVE),
                "0001",
                "TRASA GOTOWA",
                "Sprawdź trasę ewakuacji z budynku!",
                "ROUTE"
        );
    }

    public void sendNotificationIndependentEvacuation(){
        pushNotificationService.sendPushToUsers(userRepository.findTokensByEvacuationStatus(EvacuationStatus.ACTIVE),
                "0001",
                "EWAKUUJ SIĘ SAMODZIELNIE",
                "Udaj się do najbliższego wyjścia z budynku!",
                "NO-ROUTE"
        );
    }

    public void sendNotificationEvacuationCancelled(List<String> tokens){
        pushNotificationService.sendPushToUsers(tokens,
                "0001",
                "EWAKUACJA ZAKOŃCZONA",
                "Upewnij się, czy w budynku jest bezpiecznie",
                "END"
        );
    }

}
