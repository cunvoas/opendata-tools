package com.github.cunvoas.geoserviceisochrone.service.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormManualProposal;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposalMeta;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.manual.ManualParkProposalMetaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.manual.ManualParkProposalRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManualProposalServiceTest {

	@Mock
	private ManualParkProposalMetaRepository metaRepository;
	@Mock
	private ManualParkProposalRepository proposalRepository;
	@Mock
	private GeoJson2GeometryHelper geoJsonHelper;
	@Mock
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;

	@InjectMocks
	private ManualProposalService tested;

	private static final String INSEE = "69123";

	private FormManualProposal form(String mode) {
		FormManualProposal form = new FormManualProposal();
		form.setCodeInsee(INSEE);
		form.setAnnee(2024);
		form.setName("Mon parc");
		form.setMode(mode);
		form.setDescription("desc");
		return form;
	}

	private ManualParkProposalMeta meta() {
		ManualParkProposalMeta meta = new ManualParkProposalMeta();
		meta.setId(100L);
		meta.setAnnee(2024);
		meta.setInsee(INSEE);
		return meta;
	}

	@BeforeEach
	void stubRepositories() {
		when(proposalRepository.save(any(ManualParkProposal.class))).thenAnswer(inv -> inv.getArgument(0));
		when(metaRepository.save(any(ManualParkProposalMeta.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void save_creates_meta_when_missing_and_sums_surfaces() {
		FormManualProposal form = form("CIRCLE");
		form.setDiameter("10");
		ManualParkProposalMeta created = meta();
		when(metaRepository.findByAnneeAndInsee(2024, INSEE)).thenReturn(null);
		when(metaRepository.save(any(ManualParkProposalMeta.class))).thenReturn(created);

		ManualParkProposal first = new ManualParkProposal();
		first.setSurface(BigDecimal.valueOf(1500));
		ManualParkProposal second = new ManualParkProposal();
		second.setSurface(null);
		when(proposalRepository.findByIdMetaOrderByCreatedDateDesc(100L))
				.thenReturn(List.of(first, second));

		ManualParkProposal ret = tested.save(form);

		assertEquals(100L, ret.getIdMeta());
		assertEquals(Math.PI * 25, ret.getSurface().doubleValue(), 0.01);
		assertEquals(2, created.getNumberOfParks());
		assertEquals(1500, created.getTotalSurfaceOfParks());
		verify(metaRepository, org.mockito.Mockito.times(2)).save(any(ManualParkProposalMeta.class));
	}

	@Test
	void save_polygon_uses_shapely_surface_when_absent() {
		FormManualProposal form = form("POLYGON");
		form.setSGeometry("{\"type\":\"Polygon\"}");
		Geometry polygon = new GeometryFactory().createPolygon(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(0, 10), new Coordinate(10, 10),
				new Coordinate(10, 0), new Coordinate(0, 0) });
		when(metaRepository.findByAnneeAndInsee(2024, INSEE)).thenReturn(meta());
		when(geoJsonHelper.parseGeoman("{\"type\":\"Polygon\"}")).thenReturn(polygon);
		when(inseeCarre200mOnlyShapeRepository.getSurface(polygon)).thenReturn(750L);
		when(proposalRepository.findByIdMetaOrderByCreatedDateDesc(100L)).thenReturn(Collections.emptyList());

		ManualParkProposal ret = tested.save(form);

		assertNotNull(ret.getContour());
		assertNotNull(ret.getCentre());
		assertEquals(BigDecimal.valueOf(750), ret.getSurface());
	}

	@Test
	void save_polygon_keeps_explicit_surface() {
		FormManualProposal form = form("POLYGON");
		form.setSGeometry("{}");
		form.setSurface("1234.5");
		Geometry polygon = new GeometryFactory().createPolygon(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(0, 10), new Coordinate(10, 10),
				new Coordinate(10, 0), new Coordinate(0, 0) });
		when(metaRepository.findByAnneeAndInsee(2024, INSEE)).thenReturn(meta());
		when(geoJsonHelper.parseGeoman("{}")).thenReturn(polygon);
		when(proposalRepository.findByIdMetaOrderByCreatedDateDesc(100L)).thenReturn(Collections.emptyList());

		ManualParkProposal ret = tested.save(form);

		assertEquals(new BigDecimal("1234.5"), ret.getSurface());
		verify(inseeCarre200mOnlyShapeRepository, never()).getSurface(any());
	}

	@Test
	void save_reuses_existing_proposal_when_id_is_provided() {
		FormManualProposal form = form("CIRCLE");
		form.setId(9L);
		ManualParkProposal existing = new ManualParkProposal();
		existing.setId(9L);
		when(metaRepository.findByAnneeAndInsee(2024, INSEE)).thenReturn(meta());
		when(proposalRepository.findById(9L)).thenReturn(Optional.of(existing));
		when(proposalRepository.findByIdMetaOrderByCreatedDateDesc(100L)).thenReturn(Collections.emptyList());

		ManualParkProposal ret = tested.save(form);

		assertSame(existing, ret);
		assertEquals("Mon parc", ret.getName());
	}

	@Test
	void delete_removes_meta_when_last_proposal_is_deleted() {
		ManualParkProposal proposal = new ManualParkProposal();
		proposal.setId(5L);
		proposal.setIdMeta(100L);
		ManualParkProposalMeta meta = meta();
		when(proposalRepository.findById(5L)).thenReturn(Optional.of(proposal));
		when(metaRepository.findById(100L)).thenReturn(Optional.of(meta));
		when(proposalRepository.findByIdMetaOrderByCreatedDateDesc(100L)).thenReturn(Collections.emptyList());

		tested.delete(5L);

		verify(proposalRepository).delete(proposal);
		verify(metaRepository).delete(meta);
		verify(metaRepository, never()).save(any());
	}

	@Test
	void delete_recomputes_meta_when_proposals_remain() {
		ManualParkProposal proposal = new ManualParkProposal();
		proposal.setId(5L);
		proposal.setIdMeta(100L);
		ManualParkProposalMeta meta = meta();
		ManualParkProposal remaining = new ManualParkProposal();
		remaining.setSurface(BigDecimal.valueOf(10));
		when(proposalRepository.findById(5L)).thenReturn(Optional.of(proposal));
		when(metaRepository.findById(100L)).thenReturn(Optional.of(meta));
		when(proposalRepository.findByIdMetaOrderByCreatedDateDesc(100L)).thenReturn(List.of(remaining));

		tested.delete(5L);

		assertEquals(1, meta.getNumberOfParks());
		assertEquals(10, meta.getTotalSurfaceOfParks());
		verify(metaRepository).save(meta);
	}

	@Test
	void delete_ignores_unknown_proposal() {
		when(proposalRepository.findById(1L)).thenReturn(Optional.empty());

		tested.delete(1L);

		verify(metaRepository, never()).findById(any());
	}

	@Test
	void findByInsee_returns_empty_list_without_meta() {
		when(metaRepository.findByAnneeAndInsee(2021, INSEE)).thenReturn(null);

		assertTrue(tested.findByInsee(INSEE, 2021).isEmpty());
		verify(proposalRepository, never()).findByIdMetaOrderByCreatedDateDesc(any());
	}

	@Test
	void findByInsee_returns_proposals_of_meta() {
		ManualParkProposal p = new ManualParkProposal();
		when(metaRepository.findByAnneeAndInsee(2024, INSEE)).thenReturn(meta());
		when(proposalRepository.findByIdMetaOrderByCreatedDateDesc(100L)).thenReturn(List.of(p));

		assertEquals(List.of(p), tested.findByInsee(INSEE, 2024));
	}

	@Test
	void save_circle_with_lat_lng_creates_centre_point() {
		FormManualProposal form = form("CIRCLE");
		form.setMapLat("45.75");
		form.setMapLng("4.85");
		form.setSurface("100");
		when(metaRepository.findByAnneeAndInsee(2024, INSEE)).thenReturn(meta());
		when(proposalRepository.findByIdMetaOrderByCreatedDateDesc(100L)).thenReturn(Collections.emptyList());

		ManualParkProposal ret = tested.save(form);

		assertNotNull(ret.getCentre());
		assertEquals(4.85, ret.getCentre().getX(), 0.0001);
		assertEquals(45.75, ret.getCentre().getY(), 0.0001);
		assertEquals(BigDecimal.valueOf(100), ret.getSurface());
		verify(geoJsonHelper, never()).parseGeoman(anyString());
	}
}
