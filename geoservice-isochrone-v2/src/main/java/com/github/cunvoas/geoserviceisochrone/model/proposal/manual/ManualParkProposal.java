package com.github.cunvoas.geoserviceisochrone.model.proposal.manual;

import java.math.BigDecimal;
import java.util.Date;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "manual_park_proposal")
public class ManualParkProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "id_meta")
    private Long idMeta;

    private String name;

    private String mode;

    private Point centre;

    private Geometry contour;

    @Column(precision = 12, scale = 2)
    private BigDecimal surface;

    private String description;

    @Column(name = "created_date")
    private Date createdDate;
}
