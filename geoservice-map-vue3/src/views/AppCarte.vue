<template>
    <div class="text-left align-top">
        <h1 class="sr-only">Cartographie des parcs</h1>
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
                <caption class="sr-only">Légende de la carte</caption>
                <tbody>
                    <tr>
                        <th scope="row" class="pr-2 text-left font-normal">Préconisations OMS non respectées :</th>
                        <td class="px-1"><div class="w-5 aspect-square border border-gray-600" :style="`background-color: ${swatchColor(getLowColor1, 0.4)};`" /></td>
                        <td class="px-1"><div class="w-5 aspect-square border border-gray-600" :style="`background-color: ${swatchColor(getLowColor2, 0.4)};`" /></td>
                        <td class="px-1"><div class="w-5 aspect-square border border-gray-600" :style="`background-color: ${swatchColor(getLowColor3, 0.4)};`" /></td>
                    </tr>
                    
                    <tr>
                        <th scope="row" class="pr-2 text-left font-normal">Préconisations OMS respectées :</th>
                        <td class="px-1"><div class="w-5 aspect-square border border-gray-600" :style="`background-color: ${swatchColor(getHighColor1, 0.4)};`" /></td>
                        <td class="px-1"><div class="w-5 aspect-square border border-gray-600" :style="`background-color: ${swatchColor(getHighColor2, 0.4)};`" /></td>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="4">✓ : plus de 1000 m²</td>
                    </tr>
                    <tr>
                        <td colspan="4">✖ : non comptabilisé comme un parc</td>
                    </tr>

                    <tr id="legend-parks-1" v-show="showParcsLegend">
                        <th scope="row" class="pr-2 text-left font-normal">Parcs comptabilisé :</th>
                        <td class="px-1" colspan="3"><div class="w-5 aspect-square border border-gray-600" :style="`background-color: ${swatchColor(getParcComptabiliseColor, 0.5)}`" /></td>
                   </tr>
                    <tr id="legend-parks-2" v-show="showParcsLegend">
                        <th scope="row" class="pr-2 text-left font-normal">Parcs non comptabilisé :</th>
                        <td class="px-1" colspan="3"><div class="w-5 aspect-square border border-gray-600" :style="`background-color: ${swatchColor(getParcNonComptabiliseColor, 0.5)}`" /></td>
                    </tr>
                    <tr id="legend-parks-3" v-show="showParcsLegend">
                        <th scope="row" class="pr-2 text-left font-normal">Parcs futur ou détruit :</th>
                        <td class="px-1" colspan="3"><div class="w-5 aspect-square border border-gray-600" :style="`background-color: ${swatchColor(getParcFuturOuDetruitColor, 0.5)}`" /></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
import Isochrone from "../components/Isochrone.vue";
import SearchLocation from "../components/SearchLocation.vue";
import { hexToRgba } from "../utils/color.js";

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
        swatchColor(hex, alpha) {
            return hexToRgba(hex, alpha);
        },
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

.dataDetail .data-detail-title {
    margin: 0 0 5px;
    color: #767676;
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
    border: 1px solid #4b5563;
    box-sizing: border-box;
}
</style>
