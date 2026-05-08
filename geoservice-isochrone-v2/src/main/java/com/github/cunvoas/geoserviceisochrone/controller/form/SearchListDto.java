package com.github.cunvoas.geoserviceisochrone.controller.form;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * DTO for search locations.
 */
@Data
@Builder
@ToString
@EqualsAndHashCode(callSuper=false)
public class SearchListDto {
	Long id;
	String text;
	String value;
	String lat;
	String lon;
	
}
