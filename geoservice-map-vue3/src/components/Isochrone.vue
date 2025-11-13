<template>
  <div>
    <label for="annee">Année</label>
    <select id="annee" @change="onAnnee" class="form-control" v-model="annee">
      <option value="2019" selected="selected">2019</option>
      <option value="2017">2017</option>
      <option value="2015">2015</option>
    </select>

    <label for="checkbox"> | Parcs</label>
    <input id="checkbox" v-model="showParcs" type="checkbox" />

    <label for="checkbox"> | Isochrones</label>
    <input id="checkbox" v-model="showIsochrones" type="checkbox" />

    <label for="cbCarre"> | Données carroyées</label>
    <input id="cbCarre" v-model="showCarre" type="checkbox" />

    <label for="cbCadastre"> | Cadastre</label>
    <input id="cbCadastre" v-model="showCadastre" type="checkbox" />

    <label for="cbColorblind"> | Mode daltonien</label>
    <input id="cbColorblind" v-model="colorblindMode" type="checkbox" @change="onColorModeChange" />

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
          <h4>&nbsp;m²/habitant de parcs&nbsp;</h4>
          <div id="legend" class="legend">            
            <div id="legendContent" v-html="htmlLegend" />
          </div>
        </div>
       </l-control>



    <l-geo-json
        v-if="showCarre"
        :key="'carre-' + refreshKey"
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
        v-if="showParcs"
        :geojson="geojsonParcs"
        :options="detailParcs"
        :options-style="styleParcFunction"
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
// Fonction pour obtenir les couleurs selon le mode (normal ou daltonien)
function  getSquareColor(zoneDense, densite, colorblindMode = false) {
  // Gris neutre pour valeurs non calculées
  let color = '#959595';

  if (densite===null || densite==='N/A' || densite==='' ) {
    return color;
  }
  densite = (""+densite).replace(",", ".");

  // Définition des deux palettes de couleurs
  let level1, level2, level3, level4, level5;

  if (colorblindMode) {
    // Palette adaptée aux daltoniens (évite confusion rouge/vert et bleu/jaune)
    // Utilise une progression du orange (mauvais) au bleu foncé (bon)
    // ColorBrewer2 compatible avec tous types de daltonisme
    level1 = '#d73027';  // Rouge-orangé foncé (très faible)
    level2 = '#fc8d59';  // Orange clair (faible)
    level3 = '#fee090';  // Jaune-orangé pâle (moyen-faible)
    level4 = '#4575b4';  // Bleu foncé (bon)
    level5 = '#91bfdb';  // Bleu clair (très bon)
  } else {
    // Palette classique (bleu → vert)
    level1 = '#0000e8';  // Bleu foncé (très faible)
    level2 = '#6060e8';  // Bleu moyen (faible)
    level3 = '#b0b0e8';  // Bleu clair (moyen-faible)
    level4 = '#578817';  // Vert foncé (bon)
    level5 = '#57ee17';  // Vert clair (très bon)
  }

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
          color=level5;  // Excellent
      } else if (densite>=densiteMin) {
          color=level4;  // Bon
      } else {
        
        color = level3;  // Moyen
        if (densite < p25) {
          color = level1;  // Très faible
        } else if (densite < p50) {
          color = level2;  // Faible
        }
      }
  }
  return color;
}

function getColorLegend(legendeDense, colorblindMode = false) {
  
  const gradesDense = ['0','3','7','10','12'];
  const gradesSubur = ['0','8','17','25','45'];
		const labels = [];
		let from, to;
    let grades = [];

    if (legendeDense) {
      labels.push(`&nbsp; accessibles à 330 m maxi`);
      labels.push(`&nbsp;<b>Zone dense</b>`);
      grades =  gradesDense;
    } else {
      labels.push(`&nbsp; accessibles à 1200 m maxi`);
      labels.push(`&nbsp;<b>Zone périurbaine</b>`);
      grades = gradesSubur;
    }
    labels.push(`<i style="background:${getSquareColor(true, null, colorblindMode)}"></i> non calculé`);



 		for (let i = 0; i < grades.length; i++) {
			from = grades[i];
			to = grades[i + 1];

      let tSurface = `${from}${to ? `&ndash;${to}` : '+'}`
      let tColors = `<i style="opacity:0.4;background:${getSquareColor(legendeDense, from, colorblindMode)}"></i> `;
			
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
     // La légende est créée avec le mode colorblind déjà chargé depuis data()
     this.htmlLegend = getColorLegend(true, this.colorblindMode);
     console.log('Isochrone created - colorblindMode:', this.colorblindMode);
  },
  data() {
    // Récupérer le mode daltonien depuis le localStorage dès l'initialisation
    const savedColorblindMode = localStorage.getItem('colorblindMode');
    const colorblindMode = savedColorblindMode !== null ? savedColorblindMode === 'true' : false;
    
    return {
      htmlLegend: null,
      leafletMap: null,
      loading: false,
      showIsochrones: false,
      showCarre: true,
      showParcs: false,
      showCadastre: false,
      colorblindMode: colorblindMode,
      refreshKey: 0,
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
      geojsonCadastre: null,
      geojsonCarre: null,
      geojsonIsochrone: null,
      geojsonParcs: null,
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
      /**
       * Vue watcher handler for location changes.
       * Triggered when the observed location property changes.
       * @param {Object} newLocation - The updated location object.
       */
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
            
            this.callGeoJsonParcs();
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
    onColorModeChange() {
      // Sauvegarder le mode dans le localStorage
      localStorage.setItem('colorblindMode', this.colorblindMode.toString());
      
      // Mettre à jour la légende avec le nouveau mode de couleur
      this.htmlLegend = getColorLegend(this.legendeDense, this.colorblindMode);
      
      // Incrémenter refreshKey pour forcer le re-render
      this.refreshKey++;
      
      // Émettre l'événement pour informer le composant parent du changement de mode
      this.$emit('colorblind-mode-changed', this.colorblindMode);
    },
    getRootUrl() {
      const staticOnGit = 'https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main';
      const dynamicOnLocal = 'http://localhost:8980/isochrone/geolocation/';
      console.log("mode: "+MODE);

      if (MODE==='dynamic') {
        return  dynamicOnLocal;
      } else {
        return staticOnGit;
      }
    },
    isDynamic() {
      return MODE === 'dynamic';
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

        this.callGeoJsonParcs(qryPrms);
        this.callGeoJsonIsochrones(qryPrms);
        this.callGeoJsonCarres(qryPrms);
        this.callGeoJsonCadastre(qryPrms);

        const lat =(bounds._northEast.lat + bounds._southWest.lat)/2;
        const lon =(bounds._northEast.lng + bounds._southWest.lng)/2;
        
        //this.fetchCommune(lat, lon);
        this.debouncedFetchCommune(lat, lon);
        

      }
    },
    /**
     * Fetches commune information based on the provided latitude and longitude.
     * 
     * @param {number} lat - The latitude of the location.
     * @param {number} lon - The longitude of the location.
     * @returns {Promise<Object>} A promise that resolves to the commune data.
     */
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
                
                this.htmlLegend = getColorLegend(this.legendeDense, this.colorblindMode);
            }
        } else {
            
        }




        } catch (error) {
          console.error('Error fetching addresses:', error);
        }
    },
    /**
     * Debounced function to fetch commune data based on latitude and longitude.
     * Prevents excessive API calls by delaying execution until user input stabilizes.
     *
     * @param {number} lat - The latitude coordinate.
     * @param {number} lon - The longitude coordinate.
     * @returns {Promise<void>} Resolves when the commune data has been fetched.
     */
    debouncedFetchCommune: debounce(async function(lat, lon) {
      await this.fetchCommune(lat, lon);
    }, 400), // 500ms debounce delay
    async fetchGeoJson(url) {
      const response = await fetch(url);
      return await response.json();
    },


    /**
     * Debounced function to fetch isochrone data from the specified URL.
     * Executes the provided callback with the fetched data once the request completes.
     * Useful for limiting the frequency of API calls when user input changes rapidly.
     *
     * @param {string} url - The endpoint URL to fetch isochrone data from.
     * @param {Function} callback - The function to execute with the fetched data.
     */
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


  /**
   * Debounced function to fetch park data from the specified URL.
   * Executes the provided callback with the fetched data.
   * Uses debounce to limit the rate of API calls.
   *
   * @param {string} url - The endpoint URL to fetch park data from.
   * @param {Function} callback - Function to execute with the fetched data.
   */
  debouncedFetchParcs: debounce(async function(url, callback) {
    try {
      console.log("debouncedFetchParc: "+url);
      const data = await this.fetchGeoJson(url);
      callback(data);
    } catch (error) {
      console.error('Error fetching GeoJSON:', error);
    }
  }, 380), // debounce delay
  /**
   * Asynchronously fetches GeoJSON data for parks based on the provided query parameters.
   * @param {Object} qryPrms - The query parameters used to filter or request specific park data.
   * @returns {Promise<Object>} The fetched GeoJSON data for parks.
   */
  async callGeoJsonParcs(qryPrms) {
    const rootUrl = this.getRootUrl();
    let callUrl='';
    if (this.isDynamic()) {
      callUrl = `${rootUrl}/map/park/area${qryPrms}`;
    } else {
      callUrl = `${rootUrl}/geojson/parkOutline/${this.com2co}/parkOutline_${this.annee}_${this.com2co}.json`;
    }
    console.log("callGeoJsonParcs URL: "+callUrl);
    this.debouncedFetchParcs(callUrl, (data) => {
      this.geojsonParcs = data;
    });
  },


    /**
     * Debounced function to fetch data from the specified URL and execute a callback.
     * This method is wrapped with a debounce to limit the rate of execution.
     *
     * @param {string} url - The endpoint URL to fetch data from.
     * @param {Function} callback - The function to execute with the fetched data.
     */
    debouncedFetchCarre: debounce(async function(url, callback) {
      try {
        console.log("debouncedFetchCarre: "+url);
        const data = await this.fetchGeoJson(url);
        callback(data);
      } catch (error) {
        console.error('Error fetching GeoJSON:', error);
      }
    }, 400), // 400ms debounce delay
    /**
     * Asynchronously calls a service to retrieve GeoJSON data for carres (grid squares) based on the provided query parameters.
     * 
     * @param {Object} qryPrms - The query parameters used to request GeoJSON carres data.
     * @returns {Promise<Object>} - A promise that resolves to the GeoJSON data for carres.
     */
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

    /**
     * Debounced function to fetch cadastre data from the specified URL.
     * Executes the provided callback with the fetched data once the request completes.
     * Uses debounce to limit the frequency of API calls.
     *
     * @param {string} url - The endpoint URL to fetch cadastre data from.
     * @param {Function} callback - The function to execute with the fetched data.
     */
    debouncedFetchCadastre: debounce(async function(url, callback) {
      try {
        console.log("debouncedFetchCadastre: "+url);
        const data = await this.fetchGeoJson(url);
        callback(data);
      } catch (error) {
        console.error('Error fetching GeoJSON:', error);
      }
    }, 450), // debounce delay
    /**
     * Asynchronously calls the GeoJSON Cadastre service with the provided query parameters.
     * 
     * @param {Object} qryPrms - The query parameters to send to the GeoJSON Cadastre service.
     * @returns {Promise<Object>} The response data from the GeoJSON Cadastre service.
     */
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
  /**
   * Vue lifecycle hook called after the component has been mounted to the DOM.
   * Used here to add custom HTML content to the Leaflet control element.
   */
  mounted() {      
      // Add custom HTML content to the l-control
      const customControl = document.getElementById('customControl');
      if (customControl) {
        customControl.innerHTML = '<p>Custom HTML content loaded on map load</p>';
      }
      
      // Émettre le mode daltonien initial au parent (depuis localStorage)
      this.$emit('colorblind-mode-changed', this.colorblindMode);
  },
  computed: {
    /**
     * Retrieves and displays detailed information about parks within the map area.
     * Typically called after isochrone calculation to show relevant park details on the map.
     * May involve fetching data from an API or filtering existing park data.
     */
    detailParcs() {
      return {
        onEachFeature: this.onDetailPark,
      };
    },
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
          color: "#305C30",
          opacity: 0.05,
          fillColor: fillColor,
          fillOpacity: 0.01,
        };
      };
    },
    styleParcFunction() {
      //const fillColor = this.fillColor; // important! need touch fillColor in computed for re-calculate when change fillColor
      return () => {
        return {
          weight: 2,
          color: "#B08C60",
          opacity: 0.9,
          fillColor: "#608C60",
          fillOpacity: 0.3,
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
          color: "#AB7800",
          opacity: 0.95,
          fillColor: "#FFFFBB",
          fillOpacity: 0.2,
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
            " m²</div>",
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
            color: "#8eac8e",
            opacity: 0.70,
            fillColor: feature.properties.fillColor,
            fillOpacity: 0.09,
          });
        }
      };
    },

    onDetailCarre() {
      // Dépendance explicite à refreshKey pour forcer le recalcul
      const refresh = this.refreshKey;
      const colorblindMode = this.colorblindMode; // Capture du mode pour utilisation dans la closure
      console.log('onDetailCarre computed - colorblindMode:', colorblindMode, 'refreshKey:', refresh);
      return (feature, layer) => {
        layer.on("mouseover", function (e) {
          const feature = e.target.feature;
          const theComment =
            "<h4>Données carroyées</h4>" +
            "<div><b>id Inspire</b>:" +
            feature.properties.idInspire +
            "</div><div><br/><b>" +
            feature.properties.commune +
            "</b></br><b>" +
            feature.properties.people +
            "</b> habitants</div>";

          let detailData = "";
          if (
            feature.properties.surfaceTotalParkOms === null ||
            feature.properties.surfaceTotalParkOms === ""
          ) {
            detailData =
              "<div style='text-align: center'><b><i>Non calculé</i></b></div>";
          } else {
            detailData =
              "<div><b> " +
              feature.properties.popParkIncludedOms +
              "</b> ont accès et <b> " +
              feature.properties.popParkExcludedOms +
              "</b> sans</div>" +
              "<div>Surface accessible: <b>" +
              feature.properties.surfaceTotalParkOms +
              " m²</b></div>" +
              "<div>partagés avec :<b> " +
              feature.properties.popSquareShareOms +
              "  </b>hab.</div>" +
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
            fillColor:  getSquareColor(feature.properties.isDense, feature.properties.squareMtePerCapitaOms, colorblindMode),
            fillOpacity: 0.4,
          });
       

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

    onDetailPark() {
      return (feature, layer) => {
        let oms = feature.properties.oms;
        let valid =''
        if (oms === false) {
          valid = "✖";
        }

        let formattedSurface = feature.properties.surface;
        let unit = ' m²';
        
        if (feature.properties.surface) {
           if (feature.properties.surface > 1000 && oms!==false) {
            valid = "✓";
           }

          if (feature.properties.surface > 10000) {
            const surfaceInHa = feature.properties.surface / 10000;
            formattedSurface = new Intl.NumberFormat('fr-FR', { 
              minimumFractionDigits: 2, 
              maximumFractionDigits: 2 
            }).format(surfaceInHa);
            unit = ' ha';
          } else {
            formattedSurface = new Intl.NumberFormat('fr-FR').format(Math.round(feature.properties.surface));
          }
        }
        
        layer.bindTooltip(
          "<div>Nom: " + valid +" "+ feature.properties.name +
          "</div><div>Surface: " + formattedSurface + unit + 
          "</div><div>Ville: " + feature.properties.city +"</div>",
          { permanent: false, sticky: true }
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
      this.callGeoJsonParcs(""),
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
