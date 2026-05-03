package com.github.cunvoas.geoserviceisochrone;


import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.extern.overpass.OverpassGeojsonParser;
import com.github.cunvoas.geoserviceisochrone.extern.overpass.OverpassRawParser;

@SpringBootTest()
@ActiveProfiles({"dev"})
class OverpassIntegration {
    
    @Autowired
    private OverpassRawParser parserRaw;

    @Autowired
    private OverpassGeojsonParser parserGeojson;
    
    @Test
    void testParseElements_Way_withDemoGeoJson() throws Exception {
        try {
        	Path p;

        	p = Path.of("/work/PERSO/ASSO/overpass-turbo/");
            parserGeojson.parseEntityFromDirectoryPath(p);
        	
//        	p = Path.of("/work/PERSO/ASSO/overpass-turbo/mel.geojson");
//            parserGeojson.parseEntityFromFilePath(p);
        	/*
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/mel.geojson");
            parserGeojson.parseEntityFromFilePath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/lyon.geojson");
            parserGeojson.parseEntityFromFilePath(p);

            p = Path.of("/work/PERSO/ASSO/overpass-turbo/nantes.geojson");
            parserGeojson.parseEntityFromFilePath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/toulouse.geojson");
            parserGeojson.parseEntityFromFilePath(p);
            */
            
            /*
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/lyon.geo.json");
            parserRaw.parseEntityFromPath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/nantes.geo.json");
            parserRaw.parseEntityFromPath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/toulouse.geo.json");
            parserRaw.parseEntityFromPath(p);
            
            p = Path.of("/work/PERSO/ASSO/overpass-turbo/paris_grand.geo.json");
            parserRaw.parseEntityFromPath(p);
            */
        } catch (IOException e) {
            fail(e);
        }
    }

}
