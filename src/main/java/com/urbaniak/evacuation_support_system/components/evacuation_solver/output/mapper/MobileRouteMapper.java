package com.urbaniak.evacuation_support_system.components.evacuation_solver.output.mapper;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm.Node;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm.dto.AgentRoute;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Exit;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.output.dto.GridPoint;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.output.dto.MobileRoute;

import java.util.*;

public class MobileRouteMapper {

    public static MobileRoute toMobileRoute(AgentRoute route, List<Exit> exits){
        List<GridPoint> path = new ArrayList<>();
        String roomId = null;

        for(Node node : route.route()){
            if(node.isRoom()) roomId = node.roomId();
            if(node.isCorridor() && !node.isStairSegment()){
                GridPoint point = new GridPoint(node.x(), node.y(), node.floor());
                if(path.isEmpty()){
                    path.add(point);
                }else{
                    if( (path.get(path.size()-1).x() != node.x()) || (path.get(path.size()-1).y() != node.y()) || (path.get(path.size()-1).floor() != node.floor()) ){
                        if(!path.get(path.size()-1).equals(point)) {
                            path.add(point);
                        }
                    }
                }
            }
        }

        String exitName = "";
        GridPoint lastPoint = path.getLast();
        for(Exit exit : exits){
            if(lastPoint.x() == exit.x() && lastPoint.y() == exit.y() && lastPoint.floor() == exit.floor()){
                exitName = exit.name();
            }
        }

        return new MobileRoute(route.userId(), roomId, exitName, path);
    }

    public static Map<UUID, MobileRoute> toResponse(List<AgentRoute> routes, List<Exit> exits){
        Map<UUID, MobileRoute> newRoutes = new HashMap<>();

        for(AgentRoute route : routes){
            if(!route.route().isEmpty()){
                newRoutes.put(route.userId(), toMobileRoute(route, exits));
            }
        }
        return newRoutes;

    }

}
