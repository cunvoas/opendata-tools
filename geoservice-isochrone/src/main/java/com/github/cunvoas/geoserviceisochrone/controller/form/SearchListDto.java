package com.github.cunvoas.geoserviceisochrone.controller.form;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class SearchListDto {
	Long id;
	String text;
	String value;
	String lat;
	String lon;
	
}
