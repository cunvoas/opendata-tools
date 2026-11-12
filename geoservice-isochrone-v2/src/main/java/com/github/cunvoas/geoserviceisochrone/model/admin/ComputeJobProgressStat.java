package com.github.cunvoas.geoserviceisochrone.model.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for aggregated ComputeJob progress stats.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComputeJobProgressStat {
    private String epciName;
    private String cityName;
    /** Type NDVI : PARC, IRIS, CADASTRE ou CARRE. */
    private String type;
    private Integer annee;
    private Long toProcess;
    private Long inProcess;
    private Long processed;
    private Long inError;
}
