package com.urbaniak.evacuation_support_system.components.evacuationsolver.output.mapper;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.Node;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.dto.AgentRoute;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto.Exit;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.output.dto.GridPoint;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.output.dto.MobileRoute;

import java.util.ArrayList;
import java.util.List;

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

    public static List<MobileRoute> toResponse(List<AgentRoute> routes, List<Exit> exits){
        List<MobileRoute> newRoutes = new ArrayList<>();

        for(AgentRoute route : routes){
            if(!route.route().isEmpty()){
                newRoutes.add(toMobileRoute(route, exits));
            }
        }
        return newRoutes;

    }

}
