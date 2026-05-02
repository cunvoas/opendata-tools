package com.github.cunvoas.geoserviceisochrone;


import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.extern.overpass.OverpassParser;

@SpringBootTest()
@ActiveProfiles({"dev"})
class OverpassIntegration {
    
    @Autowired
    private OverpassParser parser;
    
    @Test
    void testParseElements_Way_withDemoGeoJson() throws Exception {
        try {
            Path p = Path.of("/work/PERSO/ASSO/overpass-turbo/mel.geo.json");
            parser.parseEntityFromPath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/HdF2.geo.json");
            parser.parseEntityFromPath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/lyon.geo.json");
            parser.parseEntityFromPath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/nantes.geo.json");
            parser.parseEntityFromPath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/toulouse.geo.json");
            parser.parseEntityFromPath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/paris_grand.geo.json");
            parser.parseEntityFromPath(p);
        } catch (IOException e) {
            fail(e);
        }
    }

}
