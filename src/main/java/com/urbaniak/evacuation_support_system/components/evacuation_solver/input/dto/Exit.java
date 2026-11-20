package com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto;

/**
 * Wyjście z budynku prowadzące z jednego wycinka korytarza.
 *
 * Drzwi podwójne opisuje się dwoma wyjściami o tej samej nazwie, na sąsiednich
 * wycinkach. Podpięcie przepustowości 2 do jednego wycinka nic by nie dało -
 * w wycinku mieści się jedna osoba, więc i tak przeszłaby tylko jedna.
 */
public record Exit(String name, int x, int y, int floor) {
}