package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoConstraint;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoCoordinate;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoIsoChrone;

@Component
public class DtoIsoChroneParser {

	private ObjectMapper objectMapper = new ObjectMapper();

	public DtoIsoChroneParser() {
		super();
		objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, DtoCoordinate.class);
		objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}

	public DtoIsoChrone parseIsoChrone(String json) throws JsonMappingException, JsonProcessingException {
		return objectMapper.readValue(json, DtoIsoChrone.class);
	}

	public String format(DtoIsoChrone dto) throws JsonProcessingException {
		return objectMapper.writeValueAsString(dto);
	}

	/**
	 * Parse IGN response to DTO.
	 * @param json
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	public DtoIsoChrone parseBasicIsoChrone(String json) throws JsonMappingException, JsonProcessingException {
		DtoIsoChrone dto = new DtoIsoChrone();

		JSONObject isochrone = new JSONObject(json);
		dto.setPoint(isochrone.getString("point"));
		dto.setResourceVersion(isochrone.getString("resourceVersion"));
		dto.setCostType(isochrone.getString("costType"));
		dto.setCostValue(isochrone.getInt("costValue"));
		dto.setTimeUnit(isochrone.getString("timeUnit"));
		dto.setProfile(isochrone.getString("profile"));

		JSONObject geometry = isochrone.getJSONObject("geometry");
		dto.getGeometry().setType(geometry.getString("type"));

		JSONArray coordinates = geometry.getJSONArray("coordinates");
		for (int i = 0; i < coordinates.length(); i++) {

			JSONArray sub = (JSONArray) coordinates.get(i);
			for (int j = 0; j < sub.length(); j++) {
				try {
					JSONArray coord = (JSONArray) sub.get(j);
					DtoCoordinate dtoCoord = new DtoCoordinate(coord.getDouble(0), coord.getDouble(1));
					dto.getGeometry().getCoordinates().add(dtoCoord);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		JSONArray constraints = isochrone.getJSONArray("constraints");
		if (constraints != null) {
			for (int i = 0; i < constraints.length(); i++) {
				JSONObject constraint = (JSONObject) constraints.get(i);

				DtoConstraint dtoConstraint = new DtoConstraint();
				dtoConstraint.setType(constraint.getString("type"));
				dtoConstraint.setKey(constraint.getString("key"));
				dtoConstraint.setOperator(constraint.getString("operator"));
				dtoConstraint.setValue(constraint.getString("value"));

				dto.getConstraints().add(dtoConstraint);
			}
		}

		return dto;
	}

}
