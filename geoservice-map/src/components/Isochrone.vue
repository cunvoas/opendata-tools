<template>
    <div>
        <label for="annee">Année</label>
        <select
            id="annee"
            @change="onAnnee"
            class="form-control"
            v-model="annee"
        >
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

        <!--        <label for="checkboxWithOMS" style="display: none"
            > | Conforme OMS</label
        >
        <input
            style="display: none"
            id="checkboxWithOMS"
            v-model="checkboxWithOMS"
            type="checkbox"
        />-->

        <br />

        <l-map
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

            <l-geo-json
                v-if="showIsochrones"
                :geojson="geojsonIsochrone"
                :options="detailIsochrone"
                :options-style="styleIsochroneFunction"
            />
            <l-geo-json
                v-if="showCarre"
                :geojson="geojsonCarre"
                :options="detailCarre"
                :options-style="styleCarreFunction"
            />
            <l-geo-json
                v-if="showCadastre"
                :geojson="geojsonCadastre"
                :options="detailCadastre"
                :options-style="styleCadastreFunction"
            />

            <l-control position="bottomright">
                <div id="dataDetail" class="dataDetail">
                    <h4>&nbsp;Détail des données&nbsp;</h4>
                    &nbsp;<br />&nbsp;
                </div>
            </l-control>
            <l-control-layers position="topright"></l-control-layers>
            <l-control-scale
                position="bottomleft"
                :imperial="false"
                :metric="true"
            ></l-control-scale>
        </l-map>

        <!--<br />-->
        <!--<span>Bounds: {{ bounds }}</span>-->
    </div>
</template>

<script>
import { latLng } from "leaflet";
import {
    LMap,
    LTileLayer,
    LControl,
    LControlLayers,
    LGeoJson,
    LMarker,
    LIconDefault,
    LPolygon,
    LControlScale,
} from "vue2-leaflet";

export default {
    name: "Isochrone",
    components: {
        LMap,
        LIconDefault,
        LMarker,
        LTileLayer,
        LControlLayers,
        LGeoJson,
        LPolygon,
        LControl,
        LControlScale,
    },
    data() {
        return {
            loading: false,
            showIsochrones: false,
            showCarre: true,
            showCadastre: false,
            checkboxWithOMS: true,
            zoom: 14,
            minZoom: 10,
            maxZoom: 18,
            center: [50.6349747, 3.046428],
            bounds: null,
            boundSwLat: 0,
            boundSwLng: 0,
            boundNeLat: 0,
            boundNeLng: 0,
            geojsonIsochrone: null,
            geojsonCarre: null,
            geojsonCadastre: null,
            annee: "2019",
            restUrlCadastre:
                "http://localhost:8980/isochrone/map/cadastre/area",
            restUrlCarre:
                "http://localhost:8980/isochrone/map/insee/carre200m/area",
            restUrlIsochrones: "http://localhost:8980/isochrone/map/park/area",
            fillColor: "#A0DCA0",
            tileProviders: [
                {
                    name: "Carte OpenStreetMap",
                    visible: true,
                    attribution:
                        '&copy; <a target="_blank" href="http://osm.org/copyright">OpenStreetMap</a> contributors',
                    url: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
                },
                {
                    name: "Carte WorldStreetMap",
                    visible: false,
                    url: "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}",
                    attribution: "ArcGIS World Street Map (Esri)",
                },
                {
                    name: "Satellite ArcGisOnline",
                    visible: false,
                    url: "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
                    attribution:
                        '&copy; <a href="https://www.arcgis.com/">ArcGisOnline</a> <a href="https://www.facebook.com/lmoxygene/">LM Oxygène</a> ',
                },
            ],
        };
    },
    methods: {
        onAnnee() {
            // refresh GeoJsonIsochrones v-model = this.annee
            var qryPrms =
                "?swLat=" +
                this.boundSwLat +
                "&swLng=" +
                this.boundSwLng +
                "&neLat=" +
                this.boundNeLat +
                "&neLng=" +
                this.boundNeLng +
                "&annee=" +
                this.annee;
            console.log(qryPrms);
            this.callGeoJsonIsochrones(qryPrms);
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

                var qryPrms =
                    "?swLat=" +
                    this.boundSwLat +
                    "&swLng=" +
                    this.boundSwLng +
                    "&neLat=" +
                    this.boundNeLat +
                    "&neLng=" +
                    this.boundNeLng +
                    "&annee=" +
                    this.annee;
                console.log(qryPrms);

                this.callGeoJsonIsochrones(qryPrms);
                this.callGeoJsonCarres(qryPrms);
                this.callGeoJsonCadastre(qryPrms);
            }
        },

        async callGeoJsonIsochrones(qryPrms) {
            // data isochrones
            var base =
                "https://raw.githubusercontent.com/autmel/geoservice/main/geojson/park/park_c2c_1_" +
                this.annee +
                ".json";
            //var base = this.restUrlIsochrones;
            console.log("callGeoJsonIsochrones" + base + qryPrms);
            const respIsochrone = await fetch(base + qryPrms);
            const dataIsochrone = await respIsochrone.json();
            this.geojsonIsochrone = dataIsochrone;
        },
        async callGeoJsonCarres(qryPrms) {
            // data carreau 20m
            var base =
                "https://raw.githubusercontent.com/autmel/geoservice/main/geojson/carre200m/carre200m_c2c_1_" +
                this.annee +
                ".json";
            //var base = this.restUrlCarre;
            console.log("callGeoJsonCarres" + base + qryPrms);
            const respCarre = await fetch(base + qryPrms);
            const dataCarre = await respCarre.json();
            this.geojsonCarre = dataCarre;
        },
        async callGeoJsonCadastre(qryPrms) {
            // data Cadastre
            var base =
                "https://raw.githubusercontent.com/autmel/geoservice/main/geojson/cadastre/cadastre_c2c_1.json";
            //var base = this.restUrlCadastre;
            console.log("callGeoJsonCadastre" + base + qryPrms);
            const respCadastre = await fetch(base + qryPrms);
            const dataCadastre = await respCadastre.json();
            this.geojsonCadastre = dataCadastre;
        },
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
                    opacity: 0.95,
                    fillColor: fillColor,
                    fillOpacity: 0.6,
                };
            };
        },
        styleCarreFunction() {
            return () => {
                return {
                    weight: 2,
                    color: "#24216a",
                    opacity: 0.95,
                    fillColor: "#060512",
                    fillOpacity: 0.2,
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
                    { permanent: false, sticky: true },
                );

                if (this.checkboxWithOMS && !feature.properties.oms) {
                    layer.setStyle({
                        fillColor: feature.properties.fillColor,
                        opacity: 0.0,
                        fillOpacity: 0.0,
                    });
                } else {
                    layer.setStyle({
                        fillColor: feature.properties.fillColor,
                        fillOpacity: 0.6,
                    });
                }
            };
        },

        onDetailCarre() {
            return (feature, layer) => {
                layer.on("mouseover", function (e) {
                    var feature = e.target.feature;
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

                    var detailData = "";
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
                            detailData +=
                                feature.properties.commentParks + "</i></div>";
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
            };
        },

        onDetailCadastre() {
            return (feature, layer) => {
                layer.bindTooltip(
                    "<div>INSEE:" +
                        feature.properties.idInsee +
                        "</div><div>Commune: " +
                        feature.properties.nom +
                        "</div>",
                    { permanent: false, sticky: true },
                );
            };
        },
    },
    created() {
        var self = this;
        this.loading = true;

        //  async parallel calls
        Promise.all([
            this.callGeoJsonCarres(
                self.restUrlCarre +
                    "?swLat=50.61309246538529&swLng=2.967338562011719&neLat=50.65664364875093&neLng=3.125095367431641",
            ),
            this.callGeoJsonIsochrones(
                self.restUrlIsochrones +
                    "?swLat=50.61309246538529&swLng=2.967338562011719&neLat=50.65664364875093&neLng=3.125095367431641",
            ),
            this.callGeoJsonCadastre(
                self.restUrlCadastre +
                    "?swLat=50.61309246538529&swLng=2.967338562011719&neLat=50.65664364875093&neLng=3.125095367431641",
            ),

            //    this.callGeoJsonCarres(""),
            //    this.callGeoJsonIsochrones(""),
            //    this.callGeoJsonCadastre("")
        ]).then((response) => {
            console.log(response);
            // same as : this.loading = false;
            //    but this is not reachable
            self.loading = false;
        });
    },
};
</script>
