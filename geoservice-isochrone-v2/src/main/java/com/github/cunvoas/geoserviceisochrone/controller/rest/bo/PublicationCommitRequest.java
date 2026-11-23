package com.github.cunvoas.geoserviceisochrone.controller.rest.bo;

import lombok.Data;

/**
 * Demande de commit final depuis le dashboard des exports.
 */
@Data
public class PublicationCommitRequest {
	private String token;
	private String snapshotHash;
	private String message;
}
