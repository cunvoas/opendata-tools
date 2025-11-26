<template>
    <div class="text-left align-top">
        <SearchLocation @update-location="updateLocation" @location-selected="updateLocation" :displaySearchAddress="false" />
        
        <Com2coQuickAccess 
            :currentLocation="location" 
            @graph-type-selected="updateLocation" 
        />
        
        <div class="border-2 border-transparent">
            <div class="text-center relative z-10">
                <StatsGraph 
                    :key="componentKey"
                    :location="location"
                />
            </div>
        </div>
    </div>
</template>

<script>

console.log("AppStatistics");

import SearchLocation from "../components/SearchLocation.vue";
import StatsGraph from "../components/StatsGraph.vue";
import Com2coQuickAccess from "../components/Com2coQuickAccess.vue";


export default {
    name: "AppStatistics",
    components: {
        SearchLocation,
        StatsGraph,
        Com2coQuickAccess
    },
    data() {
        return {
            location: null,
            componentKey: 0
        };
    },
    mounted() {
        // Load location from localStorage on mount
        const savedLocation = localStorage.getItem('location-selected');
        if (savedLocation) {
            try {
                this.location = JSON.parse(savedLocation);
            } catch (error) {
                console.error('Error loading saved location:', error);
                this.setDefaultLocation();
            }
        } else {
            // Set default location to Lille if no saved location
            this.setDefaultLocation();
        }
    },
    methods: {
        setDefaultLocation() {
            // Default location for Lille
            this.location = {
                cityInsee: '59350',
                regionId: '32',
                com2coId: '245900410',
                cityName: 'Lille',
                lat: 50.62925,
                lng: 3.057256
            };
        },
        async updateLocation(newLocation) {
            console.log("AppStatistics.updateLocation received:", JSON.stringify(newLocation));
            
            this.location = newLocation;
            this.componentKey++; // Force re-render
        }
    }
};
</script>
