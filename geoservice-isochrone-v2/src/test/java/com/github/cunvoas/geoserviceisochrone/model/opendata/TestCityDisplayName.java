package com.github.cunvoas.geoserviceisochrone.model.opendata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TestCityDisplayName {

    @Test
    void toTitleCaseFormatsUppercaseNames() {
        assertEquals("Lille", City.toTitleCase("LILLE"));
        assertEquals("Saint-Denis", City.toTitleCase("SAINT-DENIS"));
        assertEquals("L'Aigle", City.toTitleCase("L'AIGLE"));
        assertEquals("Aix-en-Provence", City.toTitleCase("AIX-EN-PROVENCE"));
        assertEquals("Échirolles", City.toTitleCase("ÉCHIROLLES"));
    }

    @Test
    void toTitleCaseHandlesEmptyAndNull() {
        assertNull(City.toTitleCase(null));
        assertEquals("", City.toTitleCase(""));
    }
}
