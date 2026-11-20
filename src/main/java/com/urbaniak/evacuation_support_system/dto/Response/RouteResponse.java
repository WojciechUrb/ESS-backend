package com.urbaniak.evacuation_support_system.dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.output.dto.MobileRoute;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record RouteResponse(RouteStatus status, MobileRoute route) {

    public static RouteResponse ready(MobileRoute route) {
        return new RouteResponse(RouteStatus.READY, route);
    }

    public static RouteResponse calculating() {
        return new RouteResponse(RouteStatus.CALCULATING, null);
    }

    public static RouteResponse noRoute() {
        return new RouteResponse(RouteStatus.NO_ROUTE, null);
    }

    public static RouteResponse noEvacuation() {
        return new RouteResponse(RouteStatus.NO_EVACUATION, null);
    }
}
