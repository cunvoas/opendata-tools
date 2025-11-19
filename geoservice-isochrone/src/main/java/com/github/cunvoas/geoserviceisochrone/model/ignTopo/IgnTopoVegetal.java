package com.github.cunvoas.geoserviceisochrone.model.ignTopo;


import java.util.Date;

import org.locationtech.jts.geom.Geometry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * @see https://www.insee.fr/fr/statistiques/8558420
 */
@Data
@Entity(name = "ign_topo_vegetal")
public class IgnTopoVegetal {

    /** Identifiant unique de l'entrée. */
    @Id
    @Column(name="id", length=24)
    private String id;
    
    @Column(name="insee_id", length=25)
    private String inseeId;

    @Column(name="created")
    private Date dateCreated;
    @Column(name="updated")
    private Date dateUpdated;
    @Column(name="dat_app")
    private Date dateApp;
    @Column(name="dat_conf")
    private Date dateConf;
    
    @Column(name="acqu_plani", length=25)
    private String acquPlani;
    @Column(name="prec_plani", length=25)
    private String precPlani;
    
    @Column(name="nature", length=50)
    private String nature;
    @Column(name="source", length=50)
    private String source;
    @Column(name="id_source", length=50)
    private String idSource;
    
    
    @Column(name="geometry")
    private Geometry geometry;
    


    
}
