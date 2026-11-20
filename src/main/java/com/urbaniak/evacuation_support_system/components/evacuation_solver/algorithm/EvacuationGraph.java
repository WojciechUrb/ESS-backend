package com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm.dto.AgentRoute;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm.dto.CalculationsResult;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm.dto.EvacuationPlan;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.*;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.flow.DinicMFImpl;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;


public class EvacuationGraph {

    // Ile osób wychodzi z pomieszczenia na korytarz w ciągu jednego kroku.
    static final int DOOR_CAPACITY = 1;

     // Ile osób mieści się w jednym wycinku korytarza.
    static final int CELL_CAPACITY = 1;

    static final Node SOURCE = new Node("SOURCE", 0, 0, 0, 0);
    static final Node SINK = new Node("SINK", 0, 0, 0, 0);

    private final Set<Cell> cells;
    private final List<Room> rooms;
    private final List<Exit> exits;
    private final RoomAssignment assignment;
    private final int maxTime;
    private final Graph<Node, DefaultWeightedEdge> graph;

    // Wynik algorytmu Dinica - liczony tylko raz, przy pierwszym użyciu (patrz solve()).
    // null oznacza, że algorytm jeszcze nie był uruchomiony dla tego grafu.
    private Map<DefaultWeightedEdge, Double> flowMap = null;
    private int flowValue = 0;
    private CalculationsResult calculationsResult;

    /**
     * Graf budowany jest z gotowego przypisania userów do pokoi - najpierw
     * {@link RoomAssigner#assign}, dopiero potem konstruktor i algorytm.
     * @param assignment kto w którym pokoju się znajduje (z bazy, w chwili wywołania ewakuacji)
     * @param evacuationPlan   plan symulacji ewakuacji, zwiera horyzont czasowy T - ile kroków ma algorytm na wyprowadzenie ludzi
     */
    public EvacuationGraph(BuildingLayout buildingLayout, RoomAssignment assignment, EvacuationPlan evacuationPlan) {
        if (evacuationPlan.horizon() < 1) {
            throw new IllegalArgumentException("Horyzont czasowy musi być dodatni");
        }
        // kopia zbioru - zmiana mapy z zewnątrz po zbudowaniu grafu nie wpłynie na ten obiekt
        this.cells = Set.copyOf(buildingLayout.cells());
        this.rooms = List.copyOf(buildingLayout.rooms());
        this.exits = List.copyOf(buildingLayout.exits());
        this.assignment = assignment;
        this.maxTime = evacuationPlan.horizon();
        this.graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        graph.addVertex(SOURCE);
        graph.addVertex(SINK);
        buildCorridor(buildingLayout);
        buildRooms();
        buildExits();
    }



    private void buildCorridor(BuildingLayout buildingLayout) {
        for (int t = 0; t <= maxTime; t++) {
            for (Cell cell : cells) {

                createEdge(Node.in(cell, t), Node.out(cell, t), CELL_CAPACITY);

                if (t == maxTime) {
                    continue;
                }

                // stanie w miejscu przez jeden krok
                createEdge(Node.out(cell, t), Node.in(cell, t + 1), CELL_CAPACITY);

                // przejście do sąsiedniego wycinka
                for (Cell neighbour : buildingLayout.getNeighbours(cell)) {
                    createEdge(Node.out(cell, t), Node.in(neighbour, t + 1), CELL_CAPACITY);
                }
            }
        }
    }


    private void buildRooms() {
        for (Room room : rooms) {
            List<UUID> users = assignment.usersIn(room.id());

            // pusty pokój nie dostaje węzła - nikt z niego nie wychodzi
            if (users.isEmpty()) {
                continue;
            }
            Node roomNode = Node.room(room.id());

            // przyłączanie źródeł userów do pokoju
            placeAgents(roomNode, users);

            // drzwi - jedna osoba na krok czasowy, w dowolnej chwili
            for (int t = 0; t <= maxTime; t++) {
                createEdge(roomNode, Node.in(new Cell(room.doorX(), room.doorY(), room.floor()), t), DOOR_CAPACITY);
            }
        }
    }


    private void buildExits() {
        for (Exit exit : exits) {
            for (int t = 0; t <= maxTime; t++) {
                createEdge(Node.out(new Cell(exit.x(), exit.y(), exit.floor()), t), SINK, 1);
            }
        }
    }


    private int getEvacuationTime(){
        int time = 0;
        for(DefaultWeightedEdge edge : graph.incomingEdgesOf(SINK)){
            if(flowMap.getOrDefault(edge, 0.0) > 0.5){
                Node exitNode = graph.getEdgeSource(edge);
                time = Math.max(time, exitNode.t());
            }
        }
        return time;
    }


    private Map<String, Integer> getExitsUsage(){
        Map<Cell, String> exitNames = new HashMap<>();
        Map<String, Integer> usage = new HashMap<>();

        for(Exit exit : exits){
            exitNames.put(new Cell(exit.x(), exit.y(), exit.floor()), exit.name());
        }

        for(Exit exit : exits){
            usage.put(exit.name(), 0);
        }

        for(DefaultWeightedEdge edge : graph.incomingEdgesOf(SINK)){
            if(flowMap.getOrDefault(edge, 0.0) > 0.5){
                Node exitNode = graph.getEdgeSource(edge);
                String name = exitNames.get(new Cell(exitNode.x(), exitNode.y(), exitNode.floor()));
                usage.merge(name, 1, Integer::sum);
            }
        }
        return usage;
    }

    private void createEdge(Node from, Node to, int capacity) {
        graph.addVertex(from);
        graph.addVertex(to);

        DefaultWeightedEdge edge = graph.addEdge(from, to);
        if (edge == null) {
            throw new IllegalStateException("Zduplikowana krawędź " + from + " -> " + to);
        }
        graph.setEdgeWeight(edge, capacity);
    }


    private void placeAgents(Node roomNode, List<UUID> users){
        for(UUID userId : users){
            Node agentSource = Node.agentSource(userId);
            createEdge(SOURCE, agentSource, 1);
            createEdge(agentSource, roomNode, 1);
        }
    }


    public CalculationsResult simulateEvacuation() {
        long startTime, endTime;
        if (flowMap != null) {
            return calculationsResult;
        }

        startTime = System.nanoTime();

        DinicMFImpl<Node, DefaultWeightedEdge> dinic = new DinicMFImpl<>(graph);
        double value = dinic.getMaximumFlowValue(SOURCE, SINK);
        flowValue = (int) Math.round(value);
        flowMap = dinic.getFlowMap();

        endTime = System.nanoTime();

        List<AgentRoute> routes = extractUsersRoutes();
        calculationsResult = CalculationsResult.builder()
                .evacuatedCount(getEvacuatedCount())
                .routes(routes)
                .notEvacuated(getNotEvacuated(routes))
                .evacuationTime(getEvacuationTime())
                .exitsUsage(getExitsUsage())
                .computationTime((double) (endTime - startTime)/1_000_000.0)
                .build();
        return calculationsResult;
    }

    /** Ilu ludzi zdąży opuścić budynek w zadanym horyzoncie czasowym. */
    public int getEvacuatedCount() {
        return flowValue;
    }

    /**
     * Odczytuje z przepływu trasę każdego przypisanego usera.
     * Każdy user ma krawędź SOURCE -> SOURCE:uuid o przepustowości 1:
     *  - przepływ 1 na tej krawędzi = user zdążył wyjść; idziemy za przepływem aż do SINK,
     *  - przepływ 0 = user nie zdążył w tym horyzoncie; dostaje pustą trasę (List.of()).
     */
    private List<AgentRoute> extractUsersRoutes(){

        Map<DefaultWeightedEdge, Double> remainingFlow = new HashMap<>(flowMap);
        List<AgentRoute> routes = new ArrayList<>();

        for(DefaultWeightedEdge sourceEdge : graph.outgoingEdgesOf(SOURCE)){
            Node node = graph.getEdgeTarget(sourceEdge);
            UUID agentId = node.agentUserId();

            if(remainingFlow.getOrDefault(sourceEdge, 0.0) < 0.5){
                routes.add(new AgentRoute(agentId, List.of()));
                continue;
            }

            List<Node> path = new ArrayList<>();
            Node current = node;
            path.add(current);

            while(!current.equals(SINK)){
                DefaultWeightedEdge nextEdge = null;

                for(DefaultWeightedEdge edge : graph.outgoingEdgesOf(current)){
                    double flow = remainingFlow.getOrDefault(edge, 0.0);
                    if(flow > 0.5){
                        nextEdge = edge;
                        break;
                    }
                }

                // przepływ, który wchodzi do węzła, zawsze z niego wychodzi (aż do SINK),
                // więc tu możemy trafić tylko przy błędzie w budowie grafu
                if(nextEdge == null){
                    throw new IllegalStateException( "Nie znaleziono dalszej krawędzi przepływu dla agenta "
                            + agentId
                            + " w węźle "
                            + current
                    );
                }

                remainingFlow.put(nextEdge, remainingFlow.get(nextEdge)-1);
                current = graph.getEdgeTarget(nextEdge);
                path.add(current);

            }

            routes.add(new AgentRoute(agentId, path));

        }



        return routes;
    }

    private List<UUID> getNotEvacuated(List<AgentRoute> routes){
        List<UUID> notEvacuated = new ArrayList<>();
        for(AgentRoute agentRoute : routes){
            if(agentRoute.route().isEmpty()){
                notEvacuated.add(agentRoute.userId());
            }
        }
        return notEvacuated;
    }



}
