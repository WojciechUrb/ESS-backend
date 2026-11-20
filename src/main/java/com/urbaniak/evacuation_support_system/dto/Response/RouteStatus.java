package com.urbaniak.evacuation_support_system.dto.Response;

public enum RouteStatus {
    READY,          /** trasa wyznaczona, route != null */
    CALCULATING,    /** trwa wyznaczanie tras, route == null */
    NO_ROUTE,       /** ewakuacja aktywna, ale brak trasy dla użytkownika - ewakuacja samodzielna, route == null */
    NO_EVACUATION   /** brak aktywnej ewakuacji, route == null */
}
