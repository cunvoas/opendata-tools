package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.extern.csv.CsvParkEntranceParser;
import com.github.cunvoas.geoserviceisochrone.extern.helper.UrlPointParser;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.MapperIsoChrone;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.DtoIsoChroneParser;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.IsoChroneClientService;
import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvMelParkJardinParser;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkTypeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ParkServiceTest {

	@Mock private ServiceOpenData serviceOpenData;
	@Mock private CsvParkEntranceParser csvParkEntranceParser;
	@Mock private ParkEntranceRepository parkEntranceRepository;
	@Mock private UrlPointParser gmapPointParser;
	@Mock private ParkAreaRepository parkAreaRepository;
	@Mock private ParkTypeRepository parkTypeRepository;
	@Mock private CityRepository cityRepository;
	@Mock private CsvMelParkJardinParser csvMelParkJardinParser;
	@Mock private ParkJardinRepository parkJardinRepository;
	@Mock private IsoChroneClientService clientIsoChrone;
	@Mock private DtoIsoChroneParser dtoIsoChroneParser;
	@Mock private MapperIsoChrone mapperIsoChrone;

	@InjectMocks
	private ParkService tested;

	private static ParkEntrance entrance() {
		ParkEntrance e = new ParkEntrance();
		e.setId(1L);
		return e;
	}

	@Test
	void getParkAreaById_returns_null_when_absent() {
		when(parkAreaRepository.findById(1L)).thenReturn(Optional.empty());

		assertNull(tested.getParkAreaById(1L));
	}

	@Test
	void getParkAreaById_returns_area() {
		ParkArea area = new ParkArea();
		when(parkAreaRepository.findById(1L)).thenReturn(Optional.of(area));

		assertSame(area, tested.getParkAreaById(1L));
	}

	@Test
	void getParcEtJardinById_returns_null_when_absent() {
		when(parkJardinRepository.findById(2L)).thenReturn(Optional.empty());

		assertNull(tested.getParcEtJardinById(2L));
	}

	@Test
	void saveEdited_reuses_existing_park_area() {
		ParkEntrance e = entrance();
		ParkArea existing = new ParkArea();
		existing.setId(5L);
		when(parkAreaRepository.findByIdParcEtJardin(9L)).thenReturn(existing);
		when(parkEntranceRepository.save(e)).thenReturn(e);

		ParkEntrance ret = tested.saveEdited(e, false, 9L, 2L);

		assertSame(existing, ret.getParkArea());
		assertSame(e, ret);
		verify(parkEntranceRepository).save(e);
	}

	@Test
	void saveEdited_creates_park_area_from_park() {
		ParkEntrance e = entrance();
		ParcEtJardin pj = new ParcEtJardin();
		pj.setId(9L);
		pj.setName("Parc");
		pj.setQuartier("Centre");
		pj.setTypeId(3L);
		ParkType type = new ParkType();
		ParkArea savedArea = new ParkArea();
		savedArea.setId(5L);

		when(parkAreaRepository.findByIdParcEtJardin(9L)).thenReturn(null);
		when(parkJardinRepository.findById(9L)).thenReturn(Optional.of(pj));
		when(parkTypeRepository.getReferenceById(3L)).thenReturn(type);
		when(parkAreaRepository.save(any(ParkArea.class))).thenReturn(savedArea);
		when(parkEntranceRepository.save(e)).thenReturn(e);

		ParkEntrance ret = tested.saveEdited(e, false, 9L, 2L);

		assertSame(savedArea, ret.getParkArea());
		verify(parkAreaRepository).save(any(ParkArea.class));
	}

	@Test
	void saveEdited_fails_when_park_is_unknown() {
		when(parkAreaRepository.findByIdParcEtJardin(9L)).thenReturn(null);
		when(parkJardinRepository.findById(9L)).thenReturn(Optional.empty());

		ExceptionAdmin ex = assertThrows(ExceptionAdmin.class, () -> tested.saveEdited(entrance(), false, 9L, 2L));
		assertEquals("ERR_AREA_NOT_EXITS", ex.getMessage());
		verify(parkEntranceRepository, never()).save(any());
	}

	@Test
	void merge_park_area_saves_entity() {
		ParkArea area = new ParkArea();
		when(parkAreaRepository.save(area)).thenReturn(area);

		assertSame(area, tested.mergeParkAreaEntrance(area));
	}

	@Test
	void merge_updated_entrance_areas_saves_all() {
		ParkArea a = new ParkArea();
		when(parkAreaRepository.polygonToUpdate()).thenReturn(List.of(a));

		tested.mergeUpdatedEntranceAreas();

		verify(parkAreaRepository).saveAll(List.of(a));
	}

	@Test
	void merge_null_entrance_areas_saves_all() {
		ParkArea a = new ParkArea();
		when(parkAreaRepository.polygonNull()).thenReturn(List.of(a));

		tested.mergeNullEntranceAreas();

		verify(parkAreaRepository).saveAll(List.of(a));
	}
}
