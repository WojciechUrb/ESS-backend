package com.urbaniak.evacuation_support_system.components.evacuationsolver;

import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.EvacuationPlanner;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.MapParser;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.RoomAssigner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EvacuationSolverProperties.class)
public class EvacuationSolverConfiguration {
    @Bean
    MapParser mapParser() {return new MapParser();}

    @Bean
    RoomAssigner roomAssigner() {return new RoomAssigner();}

    @Bean
    EvacuationPlanner evacuationPlanner(EvacuationSolverProperties evacuationSolverProperties){
        return new EvacuationPlanner(evacuationSolverProperties.maxHorizon());
    }

    @Bean
    EvacuationSolver EvacuationSolver(MapParser mapParser, RoomAssigner roomAssigner, EvacuationPlanner evacuationPlanner){
        return new EvacuationSolver(mapParser, roomAssigner, evacuationPlanner);
    }

}
