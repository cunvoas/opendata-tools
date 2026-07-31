package com.github.cunvoas.geoserviceisochrone.model.proposal.manual;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "manual_eval_work")
public class ManualEvalWork {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "id_meta")
    private Long idMeta;

    @Column(name = "id_inspire", length = 30)
    private String idInspire;

    @Column(name = "is_dense")
    private Boolean isDense;

    @Column(name = "local_pop", precision = 12, scale = 2)
    private BigDecimal localPopulation;

    @Column(name = "accessing_pop", precision = 12, scale = 2)
    private BigDecimal accessingPopulation;

    @Column(name = "accessing_surf", precision = 12, scale = 2)
    private BigDecimal accessingSurface;

    @Column(name = "surf_per_capita", precision = 12, scale = 2)
    private BigDecimal surfacePerCapita;

    @Column(name = "miss_surf", precision = 12, scale = 2)
    private BigDecimal missingSurface;

    @Column(name = "new_accessing_surf", precision = 12, scale = 2)
    private BigDecimal newAccessingSurface;

    @Column(name = "new_surf_per_capita", precision = 12, scale = 2)
    private BigDecimal newSurfacePerCapita;

    @Column(name = "new_miss_surf", precision = 12, scale = 2)
    private BigDecimal newMissingSurface;
}
