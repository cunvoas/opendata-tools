package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.repo.ParkTypeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

@ExtendWith(MockitoExtension.class)
class TestParkTypeService {

	@Mock
	private ParkTypeRepository parkTypeRepository;
	@Mock
	private ParkJardinRepository parkJardinRepository;
	@Mock
	private ResourceBundleMessageSource messageSource;

	@InjectMocks
	private ParkTypeService tested;

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	private static ParkType type(String i18n) {
		ParkType t = new ParkType();
		t.setId(1L);
		t.setI18n(i18n);
		return t;
	}

	@Test
	void setLabel_translates_and_reads_strict_flag() {
		ParkType t = type("park.type.parc");
		when(messageSource.getMessage(eq("park.type.parc"), any(), any(Locale.class))).thenReturn("Parc");
		when(messageSource.getMessage(eq("park.type.parc.oms.strict"), any(), any(Locale.class))).thenReturn("false");

		tested.setLabel(t);

		assertEquals("Parc", t.getLabel());
		assertEquals(Boolean.FALSE, t.getStrict());
	}

	@Test
	void setLabel_leaves_defaults_without_i18n_key() {
		ParkType t = type(null);

		tested.setLabel(t);

		assertEquals("", t.getLabel());
		assertEquals(Boolean.TRUE, t.getStrict());
		verify(messageSource, never()).getMessage(any(), any(), any(Locale.class));
	}

	@Test
	void findAll_labels_every_type() {
		ParkType a = type("a");
		ParkType b = type("b");
		when(parkTypeRepository.findAll()).thenReturn(List.of(a, b));
		when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("T");
		when(messageSource.getMessage(eq("a.oms.strict"), any(), any(Locale.class))).thenReturn("true");
		when(messageSource.getMessage(eq("b.oms.strict"), any(), any(Locale.class))).thenReturn("false");

		List<ParkType> ret = tested.findAll();

		assertEquals(2, ret.size());
		assertEquals("T", a.getLabel());
		assertEquals(Boolean.FALSE, b.getStrict());
	}

	@Test
	void get_returns_null_when_unknown() {
		when(parkTypeRepository.findById(9L)).thenReturn(Optional.empty());

		assertNull(tested.get(9L));
	}

	@Test
	void get_labels_the_found_type() {
		ParkType t = type("a");
		when(parkTypeRepository.findById(1L)).thenReturn(Optional.of(t));
		when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("T");
		when(messageSource.getMessage(eq("a.oms.strict"), any(), any(Locale.class))).thenReturn("true");

		assertSame(t, tested.get(1L));
		assertEquals("T", t.getLabel());
	}

	@Test
	void populate_fills_missing_oms_and_strict_flags() {
		ParkType t = type("a");
		ParkArea area = new ParkArea();
		area.setType(t);
		when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("T");
		when(messageSource.getMessage(eq("a.oms"), any(), any(Locale.class))).thenReturn("true");
		when(messageSource.getMessage(eq("a.oms.strict"), any(), any(Locale.class))).thenReturn("false");

		tested.populate(area);

		assertEquals(Boolean.TRUE, t.getOms());
		assertEquals(Boolean.FALSE, t.getStrict());
		assertEquals("T", t.getLabel());
	}

	@Test
	void populate_keeps_existing_flags() {
		ParkType t = type("a");
		t.setOms(Boolean.FALSE);
		t.setStrict(Boolean.FALSE);
		ParkArea area = new ParkArea();
		area.setType(t);
		when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("T");

		tested.populate(area);

		assertEquals(Boolean.FALSE, t.getOms());
		assertEquals(Boolean.FALSE, t.getStrict());
	}

	@Test
	void populate_list_ignores_null_and_empty_input() {
		tested.populate((List<ParkArea>) null);
		tested.populate(List.of());

		verify(messageSource, never()).getMessage(any(), any(), any(Locale.class));
	}

	@Test
	void populate_list_processes_each_area() {
		ParkType t = type("a");
		ParkArea area = new ParkArea();
		area.setType(t);
		when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("T");

		tested.populate(List.of(area));

		assertTrue("T".equals(t.getLabel()));
	}
}
