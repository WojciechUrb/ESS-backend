package com.urbaniak.evacuation_support_system.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Service
public class MapLoader {

    private final ResourcePatternResolver resolver =
            new PathMatchingResourcePatternResolver();

    public Set<String> loadMaps() throws IOException {
        Resource[] resources = resolver.getResources("classpath:maps/*.txt");

        Set<String> maps = new HashSet<>();

        for (Resource resource : resources) {
            try (InputStream inputStream = resource.getInputStream()) {
                String content = new String(
                        inputStream.readAllBytes(),
                        StandardCharsets.UTF_8
                );

                maps.add(content);
            }
        }
        return maps;
    }
}
