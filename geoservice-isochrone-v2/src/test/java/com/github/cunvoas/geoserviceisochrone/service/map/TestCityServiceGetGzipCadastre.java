package com.github.cunvoas.geoserviceisochrone.service.map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;

class CityServiceTest {
    @Mock
    private GeoJson2GeometryHelper geoJson2GeometryHelper;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private CadastreRepository cadastreRepository;
    @Mock
    private LaposteRepository laposteRepository;

    @InjectMocks
    private CityService cityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetGzipCadastre_returnsBytes() {
        // Arrange
        String insee = "59001";
        byte[] expected = "testdata".getBytes(StandardCharsets.UTF_8);
        // On crée une sous-classe de CityService pour surcharger doHttpGet
        CityService cityService = new CityService() {
            @Override
            protected byte[] doHttpGet(String url) {
                return expected;
            }
        };

        // Act
        byte[] result = cityService.getGzipCadastre(insee);

        // Assert
        assertArrayEquals(expected, result);
    }

    // D'autres tests peuvent être ajoutés ici (décompression, populateCities, etc.)
}