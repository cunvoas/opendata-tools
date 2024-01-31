package com.github.cunvoas.geoserviceisochrone.model.geojson;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * @author cus
 * https://www.rfc-editor.org/rfc/rfc7946
 * https://geojson.org/geojson-spec.html
 */
@Data
public class GeoJsonRoot {
	private final String type="FeatureCollection";
	private List<GeoJsonFeature> features = new ArrayList<>();
}
