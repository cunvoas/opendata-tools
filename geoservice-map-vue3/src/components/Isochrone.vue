<template>
  <div>
    <label for="annee">Année</label>
    <select id="annee" @change="onAnnee" class="form-control" v-model="annee">
      <option value="2019" selected="selected">2019</option>
      <option value="2017">2017</option>
      <option value="2015">2015</option>
    </select>

    <label for="checkbox"> | Isochrones</label>
    <input id="checkbox" v-model="showIsochrones" type="checkbox" />

    <label for="cbCarre"> | Données carroyées</label>
    <input id="cbCarre" v-model="showCarre" type="checkbox" />

    <label for="cbCadastre"> | Cadastre</label>
    <input id="cbCadastre" v-model="showCadastre" type="checkbox" />

    <br />

     

    <l-map
      ref="leafletMap"
      :zoom="zoom"
      :center="center"
      :min-zoom="minZoom"
      :max-zoom="maxZoom"
      style="height: 700px; width: 95%"
      @update:bounds="boundsUpdated"
    >
      <l-tile-layer
        v-for="tileProvider in tileProviders"
        :key="tileProvider.name"
        :name="tileProvider.name"
        :visible="tileProvider.visible"
        :url="tileProvider.url"
        :attribution="tileProvider.attribution"
        layer-type="base"
      />

      <l-control-layers position="topright" />
      <l-control position="bottomright">
        <div id="dataDetail" class="dataDetail">
          <h4>&nbsp;Détail des données&nbsp;</h4>
          &nbsp;<br />&nbsp;
        </div>
      </l-control>
      <l-control-scale
        position="bottomleft"
        :imperial="false"
        :metric="true" />

      <l-control position="bottomleft" >
        <div id="customControl" class="dataDetail">
          <h4>&nbsp;Parc accessible&nbsp;<br/>(m²/hab.)</h4>
          
          <div id="legend" class="legend">            
            <div id="legendContent" v-html="htmlLegend" />
          </div>
        </div>
       </l-control>



    <l-geo-json
        v-if="showCarre"
        :geojson="geojsonCarre"
        :options="detailCarre"
        :options-style="styleCarreFunction"
      />
      <l-geo-json
        v-if="showIsochrones"
        :geojson="geojsonIsochrone"
        :options="detailIsochrone"
        :options-style="styleIsochroneFunction"
      />
      <l-geo-json
        v-if="showCadastre"
        :geojson="geojsonCadastre"
        :options="detailCadastre"
        :options-style="styleCadastreFunction"
      />
    </l-map>

  </div>
</template>

<style>
#rotate-text-d {
  -webkit-transform: rotate(-90deg);
  -moz-transform: rotate(-90deg);
  -o-transform: rotate(-90deg);
  -ms-transform: rotate(-90deg);
  transform: rotate(-90deg);
  position: relative;
  top: 140px;
  font-style: italic bold;
  color: #000000;
}
#rotate-text-p {
  -webkit-transform: rotate(-90deg);
  -moz-transform: rotate(-90deg);
  -o-transform: rotate(-90deg);
  -ms-transform: rotate(-90deg);
  transform: rotate(-90deg);
  position: relative;
  top: 78px;
  font-style: italic bold;
  color: #000000;
}
</style>
<script>
import {
  LMap,
  LTileLayer,
  LControl,
  LControlLayers,
  LGeoJson,
  LMarker,
  LIcon,
  LPolygon,
  LControlScale,
} from "@vue-leaflet/vue-leaflet";
import L, { latLng } from "leaflet";
import "leaflet/dist/leaflet.css";
import { ref, onMounted } from 'vue';
import axios from 'axios';
import debounce from 'lodash/debounce';


const MODE='static';

// needs to be here for map coloring
function  getSquareColor(zoneDense, densite) {
  //const grey     = '#a5a5a5';
  let color     = '#959595';

  if (densite===null || densite==='N/A' || densite==='' ) {
    return color;
  }
  densite = (""+densite).replace(",", ".");

  const blue25   = '#0000e8';
  const blue50   = '#6060e8';
  const blue75   = '#b0b0e8';
  const greenMin = '#57ee17';
  const greenMax = '#578817';

  const densiteMinDense=10;
  const densiteMaxDense=12;
  const densiteMinNoDense=25;
  const densiteMaxNoDense=45;

  // default == dense
  let p25 = 3;
  let p50 = 6;
  let densiteMin = densiteMinDense;
  let densiteMax = densiteMaxDense;
  if (zoneDense===false) {
      densiteMin = densiteMinNoDense;
      densiteMax = densiteMaxNoDense;
      
      p25 = 8;
      p50 = 17;
  }


  if (zoneDense===false || zoneDense===true) {
      if (densite>=densiteMax) {
          color=greenMax;
      } else if (densite>=densiteMin) {
          color=greenMin;
      } else {
        
        color = blue75;
        if (densite < p25) {
          color = blue25;
        } else if (densite < p50) {
          color = blue50;
        }
      }
  }
  return color;
}

function getColorLegend(legendeDense) {
  
  const gradesDense = ['0','3','7','10','12'];
  const gradesSubur = ['0','8','17','25','45'];
		const labels = [];
		let from, to;
    let grades = [];

    if (legendeDense) {
      //labels.push(`<i id="rotate-text-d">dense</i> `);
      labels.push(`<i style="background:#ffffff"></i> <b>dense</b>`);
      grades =  gradesDense;
    } else {
      labels.push(`<i style="background:#ffffff"></i> <b>périurbain</b>`);
     // labels.push(`<i id="rotate-text-p">périurbain</i> `);
      grades = gradesSubur;
    }
    labels.push(`<i style="background:${getSquareColor(true,null)}"></i> non calculé`);



 		for (let i = 0; i < grades.length; i++) {
			from = grades[i];
			to = grades[i + 1];

      let tSurface = `${from}${to ? `&ndash;${to}` : '+'}`
      let tColors = `<i style="opacity:0.4;background:${getSquareColor(legendeDense, from)}"></i> `;
			
      labels.push(tColors+tSurface);

      //labels.push(dense+subur+tSurface);
      /*
      if (legendeDense) {
        labels.push(dense+tSurface);
      } else {
        labels.push(subur+tSurface);
      }
        */
		}
    

  
    return labels.join('<br>');
  }


export default {
  name: "Isochrone",
  props: {
    msg: String,
    location: Object
  },
  components: {
    LMap,
    LGeoJson,
    LPolygon,
    LIcon,
    LMarker,
    LTileLayer,
    LControl,
    LControlScale,
    LControlLayers,
  },
  
  setup() {
    const leafletMap = ref(null);
    return { leafletMap } // expose map ref
  },
  created () {
     this.htmlLegend = getColorLegend(true);
  },
  data() {
    return {
      htmlLegend: null,
      leafletMap: null,
      loading: false,
      showIsochrones: false,
      showCarre: true,
      showCadastre: false,
      legendeDense: true,
      zoom: 14,
      minZoom: 10,
      maxZoom: 18,
      center: [50.6349747, 3.046428], // Default center
      bounds: null,
      boundSwLat: 0,
      boundSwLng: 0,
      boundNeLat: 0,
      boundNeLng: 0,
      geojsonIsochrone: null,
      geojsonCarre: null,
      geojsonCadastre: null,
      annee: "2019",
      region: "9",
      com2co: "1",
      fillColor: "#A0DCA0",
      tileProviders: [
        {
          name: "Carte OpenStreetMap",
          visible: true,
          attribution:
            '&copy; <a target="_blank" href="http://osm.org/copyright">OpenStreetMap</a>',
          url: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
          maxZoom: 19,
          minZoom: 10,
        },
        {
          name: "Carte WorldStreetMap",
          visible: false,
          url: "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}",
          attribution: "ArcGIS World Street Map (Esri)",
          maxZoom: 19,
          minZoom: 10,
        },
        {
          name: "Satellite ArcGisOnline",
          visible: false,
          url: "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
          attribution:
            '&copy; <a href="https://www.arcgis.com/">ArcGisOnline</a>',
          maxZoom: 19,
          minZoom: 10,
        },
        {
          name: "Vues Aérienne IGN",
          visible: false,
          url: "https://data.geopf.fr/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/jpeg&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&LAYER=ORTHOIMAGERY.ORTHOPHOTOS",
          attribution: '&copy; <a href="https://geoservices.ign.fr/services-geoplateforme-diffusion">IGN GeoPortail</a> ',
          tileSize: 256,
          maxZoom: 18,
          minZoom: 10,
        },
      ],
      addressIcon: L.icon({
        iconUrl: '../assets/location.png', // Replace with the path to your address icon
        iconSize: [25, 41], // Size of the icon
        iconAnchor: [12, 41], // Point of the icon which will correspond to marker's location
        popupAnchor: [1, -34], // Point from which the popup should open relative to the iconAnchor
      })
    };
  },
  watch: {
    location: { 
      handler(newLocation) {
        //console.log("location handler= "+JSON.stringify(newLocation));
        if (newLocation) {

         if (newLocation.lonX && newLocation.latY) {
            
            this.center = [newLocation.latY, newLocation.lonX];
            //this.addTemporaryMarker(newLocation.latY, newLocation.lonX);
            if (newLocation.locType==='city') {
              this.zoom= 14;
            } else if (newLocation.locType==='address') {
              this.zoom= 17;
            }

            //this.fetchCommune(newLocation.latY, newLocation.lonX);
            this.debouncedFetchCommune(newLocation.latY, newLocation.lonX);
          }

          if (newLocation.regionId && this.region!==newLocation.regionId) {
            this.region=newLocation.regionId;
            
          }

          if (newLocation.com2coId && this.com2co!==newLocation.com2coId) {
            this.com2co = newLocation.com2coId;
            

            this.callGeoJsonIsochrones();
            this.callGeoJsonCarres();
            this.callGeoJsonCadastre();         
          }


        }
      },
      immediate: true,
      deep: true
    }
  },

  methods: {
    getRootUrl() {
      const staticOnGit = 'https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main';
      const dynamicOnLocal = 'http://localhost:8980/isochrone/geolocation/';

      console.log("mode: "+this.MODE);

      if (this.MODE==='dynamic') {
        return  dynamicOnLocal;
      } else {
        return staticOnGit;
      }
    },
    isDynamic() {
      return this.MODE;
    },
    detectSelfDomain() {
      let onMyServer=false;
      const siteUrl = window.location.href;
      return siteUrl.indexOf(".ovh/")>0 || siteUrl.indexOf(":5173/geolocation/")>0;
    },
    addTemporaryMarker(lat, lng) {
      /*
      const map = leafletMap.value.leafletObject
      const marker = L.marker([lat, lng], { icon: this.addressIcon, draggable:false }).addTo(map);
      setTimeout(() => {
        map.removeLayer(marker);
      }, 2000); // Remove marker after 2 seconds
      */
    },
    onAnnee() {
      // refresh GeoJsonIsochrones v-model = this.annee
      const qryPrms =
        "?swLat=" + this.boundSwLat +
        "&swLng=" + this.boundSwLng +
        "&neLat=" + this.boundNeLng +
        "&annee=" + this.annee;
      
        this.callGeoJsonIsochrones(qryPrms);
        this.callGeoJsonCarres(qryPrms);
    },
    boundsUpdated(bounds) {
      this.bounds = bounds;

      // the new map is outside the previous GeoJson calls
      if (
        bounds._southWest.lat < this.boundSwLat ||
        bounds._southWest.lng < this.boundSwLng ||
        bounds._northEast.lat > this.boundNeLat ||
        bounds._northEast.lng > this.boundNeLng
      ) {
        this.boundSwLat = bounds._southWest.lat;
        this.boundSwLng = bounds._southWest.lng;
        this.boundNeLat = bounds._northEast.lat;
        this.boundNeLng = bounds._northEast.lng;

        const qryPrms =
          "?swLat=" + this.boundSwLat +
          "&swLng=" + this.boundSwLng +
          "&neLat=" + this.boundNeLat +
          "&neLng=" + this.boundNeLng +
          "&annee=" + this.annee;
        
      console.log("boundsUpdated :"+ qryPrms);

        this.callGeoJsonIsochrones(qryPrms);
        this.callGeoJsonCarres(qryPrms);
        this.callGeoJsonCadastre(qryPrms);

        const lat =(bounds._northEast.lat + bounds._southWest.lat)/2;
        const lon =(bounds._northEast.lng + bounds._southWest.lng)/2;
        
        //this.fetchCommune(lat, lon);
        this.debouncedFetchCommune(lat, lon);
        

      }
    },
    async fetchCommune (lat, lon){
      try {

        const memoIsDense = this.legendeDense;

        // doc: https://api.gouv.fr/documentation/api-geo
        const response = await axios.get(`https://geo.api.gouv.fr/communes?fields=nom,code&format=json&lat=`+ lat +`&lon=`+ lon, { timeout: 5000 });
        const dataCommune = response.data;
        
        if (!dataCommune || !dataCommune.length) {
            
            return;
        }
        const codeInsee = dataCommune[0].code;
        


        // Get density data
        const respDensite = await axios.get("https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/cities/densite.json", { timeout: 5000 });
        const dataDensite = respDensite.data;

        // Find the density data for the given codeInsee
        const densityItem = dataDensite.find(item => item[codeInsee] !== undefined);
        if (densityItem) {
            const codeDensite = densityItem[codeInsee];
            

            // Update legend based on density code
            this.legendeDense = (codeDensite === "1" || codeDensite === "2");

            // Update legend if density classification changed
            if (memoIsDense !== this.legendeDense) {
                
                this.htmlLegend = getColorLegend(this.legendeDense);
            }
        } else {
            
        }




        } catch (error) {
          console.error('Error fetching addresses:', error);
        }
    },
    debouncedFetchCommune: debounce(async function(lat, lon) {
      await this.fetchCommune(lat, lon);
    }, 400), // 500ms debounce delay
    async fetchGeoJson(url) {
      const response = await fetch(url);
      return await response.json();
    },

    debouncedFetchIsochrone: debounce(async function(url, callback) {
      try {
        console.log("debouncedFetchIsochrone: "+url);
        const data = await this.fetchGeoJson(url);
        callback(data);
      } catch (error) {
        console.error('Error fetching GeoJSON:', error);
      }
    }, 380), // debounce delay
    async callGeoJsonIsochrones(qryPrms) {
      const rootUrl = this.getRootUrl();
      let callUrl='';
      if (this.isDynamic()) {
        callUrl = `${rootUrl}/map/park/area${qryPrms}`;
      } else {
        callUrl = `${rootUrl}/geojson/isochrones/${this.com2co}/isochrone_${this.annee}_${this.com2co}.json`;
      }
      this.debouncedFetchIsochrone(callUrl, (data) => {
        this.geojsonIsochrone = data;
      });
    },

    debouncedFetchCarre: debounce(async function(url, callback) {
      try {
        console.log("debouncedFetchCarre: "+url);
        const data = await this.fetchGeoJson(url);
        callback(data);
      } catch (error) {
        console.error('Error fetching GeoJSON:', error);
      }
    }, 400), // 400ms debounce delay
    async callGeoJsonCarres(qryPrms) {
      const rootUrl = this.getRootUrl();
      let callUrl='';
      if (this.isDynamic()) {
        callUrl = `${rootUrl}/map/insee/carre200m/area${qryPrms}`;
      } else {
        callUrl = `${rootUrl}/geojson/carres/${this.com2co}/carre_${this.annee}_${this.com2co}.json`;
      }
      this.debouncedFetchCarre(callUrl, (data) => {
        this.geojsonCarre = data;
      });
    },

    debouncedFetchCadastre: debounce(async function(url, callback) {
      try {
        console.log("debouncedFetchCadastre: "+url);
        const data = await this.fetchGeoJson(url);
        callback(data);
      } catch (error) {
        console.error('Error fetching GeoJSON:', error);
      }
    }, 450), // debounce delay
    async callGeoJsonCadastre(qryPrms) {
      const rootUrl = this.getRootUrl();
      let callUrl='';
      if (this.isDynamic()) {
        callUrl = `${rootUrl}/map/cadastre/area${qryPrms}`;
      } else {
        callUrl = `${rootUrl}/data/cadastres/${this.region}/cadastre_c2c_${this.com2co}.json`;
      }
      this.debouncedFetchCadastre(callUrl, (data) => {
        this.geojsonCadastre = data;
      });
    },

  },
  mounted() {      // Add custom HTML content to the l-control
      const customControl = document.getElementById('customControl');
      if (customControl) {
        customControl.innerHTML = '<p>Custom HTML content loaded on map load</p>';
      }
  },
  computed: {
    detailIsochrone() {
      return {
        onEachFeature: this.onDetailIsochrone,
      };
    },
    detailCadastre() {
      return {
        onEachFeature: this.onDetailCadastre,
      };
    },
    detailCarre() {
      return {
        onEachFeature: this.onDetailCarre,
      };
    },
    styleIsochroneFunction() {
      const fillColor = this.fillColor; // important! need touch fillColor in computed for re-calculate when change fillColor
      return () => {
        return {
          weight: 2,
          color: "#406C40",
          opacity: 0.15,
          fillColor: fillColor,
          fillOpacity: 0.1,
        };
      };
    },
    styleCarreFunction() {
      return () => {
        return {
          weight: 1,
          color: "#24216a",
          opacity: 0.20,
          fillColor: "#060512",
          fillOpacity: 0.15,
        };
      };
    },
    styleCadastreFunction() {
      return () => {
        return {
          weight: 2,
          color: "#CB9800",
          opacity: 0.95,
          fillColor: "#FFFF99",
          fillOpacity: 0.3,
        };
      };
    },
    onDetailIsochrone() {
      return (feature, layer) => {
        layer.bindTooltip(
          "<div>Quartier:" +
            feature.properties.quartier +
            "</div><div>" +
            feature.properties.name +
            " (id:" +
            feature.properties.id +
            ")</div><div>" +
            feature.properties.people +
            " hab. pour " +
            feature.properties.area +
            " m² (" +
            feature.properties.areaPerPeople +
            " m²/h)</div>",
          { permanent: false, sticky: true }
        );

        if ( !feature.properties.oms) {
          layer.setStyle({
            fillColor: feature.properties.fillColor,
            opacity: 0.0,
            fillOpacity: 0.0,
          });
        } else {
          layer.setStyle({
            weight: 2,
            color: "#406C40",
            opacity: 0.90,
            fillColor: feature.properties.fillColor,
            fillOpacity: 0.09,
          });
        }
      };
    },

    onDetailCarre() {
      return (feature, layer) => {
        layer.on("mouseover", function (e) {
          const feature = e.target.feature;
          const theComment =
            "<h4>Données carroyées : Parc / Habitant</h4>" +
            "<div>id Inspire:" +
            feature.properties.idInspire +
            "</div>" +
            "<div>Commune: <b>" +
            feature.properties.commune +
            "</b>" +
            ", pop.: <b>" +
            feature.properties.people +
            "</b></div>";

          let detailData = "";
          if (
            feature.properties.surfaceTotalParkOms === null ||
            feature.properties.surfaceTotalParkOms === ""
          ) {
            detailData =
              "<div style='text-align: center'><b><i>Non calculé</i></b></div>";
          } else {
            detailData =
              "<div>ont accès: " +
              feature.properties.popParkIncludedOms +
              " pers. (sans: " +
              feature.properties.popParkExcludedOms +
              ")</div>" +
              "<div>Surface parcs: " +
              feature.properties.surfaceTotalParkOms +
              " m²</div>" +
              "<div>Partagés avec : " +
              feature.properties.popSquareShareOms +
              " pers.</div>" +
              "<div>Soit : <b>" +
              feature.properties.squareMtePerCapitaOms +
              " m²/hab</b></div>" +
              "<div style='padding-top: 1em;'><i><u>Parcs accessibles:</u><br />";
            if (feature.properties.commentParks !== "") {
              detailData += feature.properties.commentParks + "</i></div>";
            } else {
              detailData += "Aucun</i></div>";
            }
          }

          e.target.setStyle({
            weight: 5,
          });

          document.getElementById("dataDetail").innerHTML =
            theComment + detailData;
        });

        layer.on("mouseout", function (e) {
          e.target.setStyle({
            weight: 2,
          });
        });

        layer.setStyle({
            fillColor:  getSquareColor(feature.properties.isDense, feature.properties.squareMtePerCapitaOms),
            fillOpacity: 0.4,
          });
       

        /*
        if (feature.properties.popParkIncluded !== "n/a") {
          layer.setStyle({
            fillColor: feature.properties.fillColor,
            fillOpacity: 0.6,
          });
          // cas non calculé
        } else if (
          feature.properties.surfaceTotalParkOms === null ||
          feature.properties.surfaceTotalParkOms === ""
        ) {
          layer.setStyle({
            fillColor: "#4944f5",
            fillOpacity: 0.2,
          });
        }
        */
      };
    },

    onDetailCadastre() {
      return (feature, layer) => {
        layer.bindTooltip(
          "<div>INSEE: " + feature.properties.idInsee +
          "</div><div>Commune: " + feature.properties.nom +
           "</div>",
          //{ permanent: false, sticky: true }
        );
      };
    },
  },
  beforeMount() {
    
    const self = this;
    self.loading = true;

    //  async parallel calls
    Promise.all([
      /*
      this.callGeoJsonCarres(
        self.restUrlCarre +
          "?swLat=50.61309246538529&swLng=2.967338562011719&neLat=50.65664364875093&neLng=3.125095367431641"
      ),
      self.callGeoJsonIsochrones(""),
      this.callGeoJsonCadastre(
        self.restUrlCadastre +
          "?swLat=50.61309246538529&swLng=2.967338562011719&neLat=50.65664364875093&neLng=3.125095367431641"
      ),
      */
      this.callGeoJsonCarres(""),
      this.callGeoJsonIsochrones(""),
      this.callGeoJsonCadastre(""),
    ]).then((response) => {
      
      // same as : this.loading = false;
      //    but this is not reachable
      self.loading = false;
    });
  },
};
</script>
