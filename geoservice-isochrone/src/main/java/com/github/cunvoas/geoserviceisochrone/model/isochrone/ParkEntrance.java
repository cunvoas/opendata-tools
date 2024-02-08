package com.github.cunvoas.geoserviceisochrone.model.isochrone;



import java.util.Date;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity(name = "park_entrance")
public class ParkEntrance {

        @Id
        @Column(name="id")
        @GeneratedValue(
        	    strategy = GenerationType.SEQUENCE,
        	    generator = "seq_park_entrance"
        	)
        private long id;
        
        @ManyToOne
        @JoinColumn(name="area_id", nullable=false)
        private ParkArea parkArea;

        // JSON response
        @Column(name="ign_response", length=4000)
        private String ignReponse;

        @Column(name="update_date")
        private Date updateDate;
        
        @Column(name="ign_date")
        private Date ignDate;
        
        @Column(name="description")
        private String description;
        
        @Column(name="entrance_link", length=1000)
        private String entranceLink;

        @Column(name="entrance_point", columnDefinition = "geometry(Point,4326)")
        private Point entrancePoint;

        /**
         * IGN isochrine
         */
        @Column(columnDefinition = "geometry(Polygon,4326)")
        private Polygon polygon;

        
        public String getEntryLng() {
        	if (entrancePoint!=null && entrancePoint.getCoordinate()!=null) {
        		return String.valueOf( String.valueOf(entrancePoint.getCoordinates()[0].x) );
        	}
        	return "";
        }
        
        public String getEntryLat() {
        	if (entrancePoint!=null && entrancePoint.getCoordinate()!=null) {
        		return String.valueOf( String.valueOf(entrancePoint.getCoordinates()[0].y) );
        	}
        	return "";
        }

}
