<template>
    <div id="appCarte" valign="top" align="left">
        <SearchLocation @update-location="updateLocation" @location-selected="updateLocation" />
        
        <span style="border: 2px">
            <div
                id="map"
                align="center"
                valign="middle"
                style="position: relative, z-index: 10;"
            >
                <Isochrone msg="Cartographie des parcs" :location="location" />
            </div>
        </span>

        <!-- <LegendeCarte></LegendeCarte> -->
    
    </div>
</template>

<script>
import HeaderAsso from "../components/HeaderAsso.vue";
import Isochrone from "../components/Isochrone.vue";
import LegendeCarte from "../components/LegendeCarte.vue";
import SearchLocation from "../components/SearchLocation.vue";

export default {
    name: "AppCarte",
    components: {
        HeaderAsso,
        Isochrone,
        LegendeCarte,
        SearchLocation
    },
    data() {
        return {
            location: null
        };
    },
    methods: {
        updateLocation(newLocation) {
            //alert(' appcarte. updateLocation(newLocation) '+JSON.stringify(newLocation) );
            console.log("updateLocation= "+JSON.stringify(newLocation))
        this.location = newLocation;
        }
    }
};
</script>

<style>
#app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 10px;
}
table.legend td {
    color: #ffffff;
    font-size: 12px;
    padding: 2px;
}

.dataDetail {
    padding: 6px 8px;
    font:
        16px/18px Arial,
        Helvetica,
        sans-serif;
    background: white;
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
    border-radius: 5px;
}
.dataDetail h4 {
    margin: 0 0 5px;
    color: #777;
}
.legend {
    text-align: left;
    line-height: 18px;
    color: #555;
}
.legend i {
    width: 18px;
    height: 18px;
    float: left;
    margin-right: 8px;
    opacity: 0.7;
}
</style>
