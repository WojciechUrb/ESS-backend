package com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm;

import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto.Cell;

import java.util.UUID;

/**
 * kind IN/OUT - wycinek korytarza.
 * ROOM:id - pomieszczenie. id to numer pomieszczenia np.007 (pietro np. 0 + nr pokoju np. 07)
 * SURCE:uuid - źródło agenta reprezentującego użytkownika,
 * SOURCE / SINK
 */
public record Node(String kind, int x, int y, int floor, int t) {

    static Node in(Cell cell, int t) {
        return new Node("IN", cell.x(), cell.y(), cell.floor(), t);
    }

    static Node out(Cell cell, int t) {
        return new Node("OUT", cell.x(), cell.y(), cell.floor(), t);
    }

    static Node room(String roomId) {
        return new Node("ROOM:" + roomId, 0, 0, 0, 0);
    }

    /** Źródło pojedynczego usera - kluczem jest jego UUID z bazy, więc id muszą być unikalne. */
    static Node agentSource(UUID userId) {
        return new Node(AGENT_SOURCE_PREFIX + userId, 0, 0, 0, 0);
    }

    static final String AGENT_SOURCE_PREFIX = "SOURCE:";

    /** czy węzeł jest źródłem konkretnego usera (a nie wspólnym SOURCE). */
    boolean isAgentSource() {
        return kind.startsWith(AGENT_SOURCE_PREFIX);
    }

    /** UUID usera zakodowany w węźle źródła; wolno wołać tylko gdy {@link #isAgentSource()}. */
    UUID agentUserId() {
        return UUID.fromString(kind.substring(AGENT_SOURCE_PREFIX.length()));
    }

    @Override
    public String toString() {
        if (kind.startsWith("ROOM:") || isAgentSource() || kind.equals("SOURCE") || kind.equals("SINK")) {
            return kind;
        }
        return kind + "(" + x + "," + y + ",t=" + t + ")";
    }

    public boolean isCorridor(){
        return kind.equals("IN") || kind.equals("OUT");
    }

    public boolean isRoom(){
        return kind.startsWith("ROOM:");
    }

    public boolean isStairSegment(){
        return isCorridor() && floor == Cell.STAIRS;
    }

    public String roomId(){
        return kind.substring("ROOM:".length());
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int t() {
        return t;
    }

    @Override
    public int floor() {
        return floor;
    }

    @Override
    public String kind() {
        return kind;
    }
}
