package com.github.cunvoas.geoserviceisochrone.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LogSanitizerTest {

    @Test
    public void sanitize_shouldReplaceCrLfAndTab() {
        assertEquals("a_b_c_d", LogSanitizer.sanitize("a\nb\rc\td"));
    }
}
