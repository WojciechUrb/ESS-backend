package com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.dto;


import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.Node;

import java.util.List;
import java.util.UUID;

/**
 * Trasa jednego usera odczytana z przepływu: od jego źródła, przez pokój i korytarz, do SINK.
 *
 * @param userId id usera z bazy - po nim backend odnajduje trasę konkretnej osoby
 * @param route  kolejne węzły grafu na trasie
 */

public record AgentRoute(UUID userId, List<Node> route) {
//    public AgentRoute(UUID userId, List<Node> route){
//        this.userId = userId;
//        this.route = List.copyOf(route);
//    }
}