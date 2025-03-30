<template>
    <div id="appStatistics" valign="top" align="left">
        
        <SearchLocation @update-location="updateLocation" @location-selected="updateLocation" :displaySearchAddress="false" />
        
        <span style="border: 2px">
            <div
                id="statsGrapg"
                align="center"
                valign="middle"
                style="position: relative, z-index: 10;"
            >
                <StatsGraph 
                    :key="componentKey"
                    :location="location"
                    />
            </div>
            
        </span>
    
    </div>
</template>

<script>

console.log("AppStatistics");

import SearchLocation from "../components/SearchLocation.vue";
import StatsGraph from "../components/StatsGraph.vue";


export default {
    name: "AppStatistics",
    components: {
        SearchLocation,
        StatsGraph
    },
    data() {
        return {
            location: null,
            componentKey: 0
        };
    },
    methods: {
        async updateLocation(newLocation) {
            //console.log("AppStatistics.updateLocation", JSON.stringify(newLocation));
            //this.location = JSON.parse(JSON.stringify(newLocation));
            
            this.location = newLocation;
            this.componentKey++; // Force re-render
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

</style>
