<template>
    <div class="text-left align-top">
        <SearchLocation @update-location="updateLocation" @location-selected="updateLocation" :displaySearchAddress="true" />
        
        <div class="border-2 border-transparent mt-4">
            <div class="text-center relative z-10">
                <Isochrone msg="Cartographie des parcs" :location="location" @colorblind-mode-changed="updateColorblindMode" @parcs-visibility-changed="updateParcsLegend" />
            </div>
        </div>
    </div>
    
    <div class="mt-4">   
        <div class="border-2 border-transparent">
            <table class="text-left">
                <tbody>
                    <tr>
                        <td class="pr-2">Préconisations OMS non respectées :</td>
                        <td class="px-1"><div class="w-5 aspect-square opacity-40" :style="`background-color: ${getLowColor1};`" /></td>
                        <td class="px-1"><div class="w-5 aspect-square opacity-40" :style="`background-color: ${getLowColor2};`" /></td>
                        <td class="px-1"><div class="w-5 aspect-square opacity-40" :style="`background-color: ${getLowColor3};`" /></td>
                    </tr>
                    
                    <tr>
                        <td class="pr-2">Préconisations OMS respectées :</td>
                        <td class="px-1"><div class="w-5 aspect-square opacity-40" :style="`background-color: ${getHighColor1};`" /></td>
                        <td class="px-1"><div class="w-5 aspect-square opacity-40" :style="`background-color: ${getHighColor2};`" /></td>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="4">✓ : plus de 1000 m²</td>
                    </tr>
                    <tr>
                        <td colspan="4">✖ : non comptabilisé comme un parc</td>
                    </tr>

                    <tr id="legend-parks-1" v-show="showParcsLegend">
                        <td class="pr-2">Parcs comptabilisé :</td>
                        <td class="px-1" colspan="3"><div class="w-5 aspect-square opacity-40" :style="`background-color: ${getParcComptabiliseColor}`" /></td>
                   </tr>
                    <tr id="legend-parks-2" v-show="showParcsLegend">
                        <td class="pr-2" >Parcs non comptabilisé :</td>
                        <td class="px-1" colspan="3"><div class="w-5 aspect-square opacity-40" :style="`background-color: ${getParcNonComptabiliseColor}`" /></td>
                    </tr>
                    <tr id="legend-parks-3" v-show="showParcsLegend">
                        <td class="pr-2" >Parcs futur ou détruit :</td>
                        <td class="px-1" colspan="3"><div class="w-5 aspect-square opacity-40" :style="`background-color: ${getParcFuturOuDetruitColor}`" /></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
import Isochrone from "../components/Isochrone.vue";
import SearchLocation from "../components/SearchLocation.vue";

export default {
    name: "AppCarte",
    components: {
        SearchLocation,
        Isochrone
    },
    data() {
        return {
            location: null,
            colorblindMode: false,
            showParcsLegend: false
        };
    },
    computed: {
        getLowColor1() {
            return this.colorblindMode ? '#d73027' : '#0000e8';
        },
        getLowColor2() {
            return this.colorblindMode ? '#fc8d59' : '#6060e8';
        },
        getLowColor3() {
            return this.colorblindMode ? '#fee090' : '#b0b0e8';
        },
        getHighColor1() {
            return this.colorblindMode ? '#91bfdb' : '#57ee17';
        },
        getHighColor2() {
            return this.colorblindMode ? '#4575b4' : '#578817';
        },
        getParcComptabiliseColor() {
            return this.colorblindMode ? '#2CA02C' : '#3aa637';
        },
        getParcNonComptabiliseColor() {
            return this.colorblindMode ? '#FF7F0E' : '#e96020';
        },
        getParcFuturOuDetruitColor() {
            return this.colorblindMode ? '#9467BD' : '#DC20E9';
        }
    },
    methods: {
        updateLocation(newLocation) {
            //alert(' appcarte. updateLocation(newLocation) '+JSON.stringify(newLocation) );
            
        this.location = newLocation;
        },
        updateColorblindMode(isColorblindMode) {
            this.colorblindMode = isColorblindMode;
        },
        updateParcsLegend(isShowParcs) {
            this.showParcsLegend = isShowParcs;
        }
    }
};
</script>

<style>
/* Keep Leaflet-specific styles */
.dataDetail {
    padding: 6px 8px;
    font: 16px/18px Arial, Helvetica, sans-serif;
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
