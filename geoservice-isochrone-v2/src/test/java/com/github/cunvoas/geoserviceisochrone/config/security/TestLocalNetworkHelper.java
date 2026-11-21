package com.github.cunvoas.geoserviceisochrone.config.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LocalNetworkHelperTest {

    @Test
    public void matches_shouldAcceptWhitelistAndPrivateRangesOnly() {
        assertTrue(LocalNetworkHelper.matches("192.168.1"));
        assertTrue(LocalNetworkHelper.matches("10.42.7"));
        assertTrue(LocalNetworkHelper.matches("172.20.3"));
        assertFalse(LocalNetworkHelper.matches("172.32.0"));
        assertFalse(LocalNetworkHelper.matches("8.8.8"));
    }
}
