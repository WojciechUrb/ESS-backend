package com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.dto;

/**
 * Wynik planowania ewakuacji.
 *
 * @param horizon        górne ograniczenie trwania symulacji ewakuacji;
 * @param reachedLimit   czy maksymalne ograniczenie (DEAULT_MAx_HORIZON) czasu trwania symulacji zostało osiągnięte, optymalnie false
 */
public record EvacuationPlan(int horizon, boolean reachedLimit, double computationTime) {


}
