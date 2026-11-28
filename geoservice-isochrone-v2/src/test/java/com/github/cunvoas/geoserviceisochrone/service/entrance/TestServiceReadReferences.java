package com.github.cunvoas.geoserviceisochrone.service.entrance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeDensiteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeDensiteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParcPrefectureRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestServiceReadReferences {

	@Mock private ApplicationBusinessProperties applicationBusinessProperties;
	@Mock private RegionRepository regionRepository;
	@Mock private CommunauteCommuneRepository communauteCommuneRepository;
	@Mock private CityRepository cityRepository;
	@Mock private CadastreRepository cadastreRepository;
	@Mock private LaposteRepository laposteRepository;
	@Mock private ParkJardinRepository parkJardinRepository;
	@Mock private ParkAreaRepository parkAreaRepository;
	@Mock private ParkAreaComputedRepository parkAreaComputedRepository;
	@Mock private ParkEntranceRepository parkEntranceRepository;
	@Mock private ParcPrefectureRepository parcPrefectureRepository;
	@Mock private InseeDensiteCommuneRepository inseeDensiteCommuneRepository;

	@InjectMocks
	private ServiceReadReferences tested;

	private static City city(Long id, String insee) {
		City c = new City();
		c.setId(id);
		c.setInseeCode(insee);
		return c;
	}

	@Test
	void getRegion_delegates_to_repository() {
		List<Region> regions = List.of(new Region());
		when(regionRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(regions);

		assertEquals(regions, tested.getRegion());
	}

	@Test
	void getCityById_returns_null_when_unknown() {
		when(cityRepository.findById(3L)).thenReturn(Optional.empty());

		assertNull(tested.getCityById(3L));
	}

	@Test
	void getCityById_returns_city() {
		City c = city(3L, "69123");
		when(cityRepository.findById(3L)).thenReturn(Optional.of(c));

		assertSame(c, tested.getCityById(3L));
	}

	@Test
	void getCityByRegionId_returns_empty_when_no_city() {
		when(cityRepository.findByRegionId(84L)).thenReturn(List.of());

		assertTrue(tested.getCityByRegionId(84L).isEmpty());
	}

	@Test
	void getCommunauteCommuneByRegionId_delegates() {
		List<CommunauteCommune> list = List.of(new CommunauteCommune());
		when(communauteCommuneRepository.findByRegionId(84L)).thenReturn(list);

		assertEquals(list, tested.getCommunauteCommuneByRegionId(84L));
	}

	@Test
	void park_area_lookups_return_null_when_absent() {
		when(parkAreaRepository.findById(1L)).thenReturn(Optional.empty());
		when(parkAreaRepository.findByIdParcEtJardin(2L)).thenReturn(null);

		assertNull(tested.getParkAreaById(1L));
		assertNull(tested.getByIdParcEtJardin(2L));
	}

	@Test
	void parc_et_jardin_lookup_returns_entity() {
		ParcEtJardin p = new ParcEtJardin();
		p.setId(7L);
		when(parkJardinRepository.findById(7L)).thenReturn(Optional.of(p));

		assertSame(p, tested.getParcEtJardinById(7L));
	}

	@Test
	void isCityDense_returns_null_for_unknown_city() {
		assertNull(tested.isCityDense(null));
		when(cityRepository.findById(9L)).thenReturn(Optional.empty());
		assertNull(tested.isCityDense(9L));
	}

	@Test
	void isCityDense_true_when_code_is_configured_as_dense() {
		when(cityRepository.findById(9L)).thenReturn(Optional.of(city(9L, "69123")));
		InseeDensiteCommune densite = new InseeDensiteCommune();
		densite.setCodeDensite("11");
		when(inseeDensiteCommuneRepository.findById("69123")).thenReturn(Optional.of(densite));
		when(applicationBusinessProperties.getInseeCodeDensite()).thenReturn("11,12");

		assertTrue(tested.isCityDense(9L));
	}

	@Test
	void isCityDense_false_when_code_is_not_dense() {
		when(cityRepository.findById(9L)).thenReturn(Optional.of(city(9L, "69123")));
		InseeDensiteCommune densite = new InseeDensiteCommune();
		densite.setCodeDensite("30");
		when(inseeDensiteCommuneRepository.findById("69123")).thenReturn(Optional.of(densite));
		when(applicationBusinessProperties.getInseeCodeDensite()).thenReturn("11,12");

		assertEquals(Boolean.FALSE, tested.isCityDense(9L));
	}

	@Test
	void metropole_lookup_returns_null_for_unknown_id() {
		when(communauteCommuneRepository.findById(5L)).thenReturn(Optional.empty());

		assertNull(tested.getCommunauteCommuneById(5L));
	}

	@Test
	void getCoordinate_returns_null_for_null_id() {
		assertNull(tested.getCoordinate(null));
	}

	@Test
	void entrance_lookups_delegate() {
		when(parkEntranceRepository.findByParkId(1L)).thenReturn(List.of());

		assertTrue(tested.getEntranceByParkId(1L).isEmpty());
	}

	@Test
	void getEntranceById_returns_null_when_absent() {
		when(parkEntranceRepository.findById(2L)).thenReturn(Optional.empty());

		assertNull(tested.getEntranceById(2L));
	}
}
