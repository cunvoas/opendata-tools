<template>
    <div>
        <select v-model="selectedRegion" @change="fetchCom2cos" tabindex="1">
            <option v-for="region in regions" :key="region.id" :value="region.id">
                {{ region.name }}
            </option>
        </select>
        &nbsp;
        <select v-model="selectedCom2co" @change="fetchCities" :disabled="!selectedRegion" tabindex="2">
            <option v-for="com2co in com2cos" :key="com2co.id" :value="com2co.id">
                {{ com2co.name }}
            </option>
        </select>
        &nbsp;
        <select v-model="selectedCity" @change="handleCityChange" :disabled="!selectedCom2co" tabindex="3">
            <option v-for="city in cities" :key="city.id" :value="city.id" :data-insee-code="city.inseeCode" :data-longitude-x="city.lonX" :data-latitude-y="city.latY">
                {{ city.name }} ({{ city.inseeCode }})
            </option>
        </select>
        &nbsp;
        <Autocomplete v-model="selectedAddress" :fetch-items="fetchAddresses" placeholder="Aller à une adresse"
            :disabled="!selectedCity" tabindex="4" @location-selected="handleLocationSelected" />

    </div>
    &nbsp;

</template>

<script>
import axios from 'axios';
import debounce from 'lodash/debounce';
import Autocomplete from './Autocomplete.vue';
import { useCookies } from "vue3-cookies";

export default {
    components: { Autocomplete },
    data() {
        return {
            regions: [],
            com2cos: [],
            cities: [],
            addresses: [],
            selectedRegion: null,
            selectedCom2co: null,
            selectedCom2coName: null,
            selectedCity: null,
            selectedCityName: null,
            selectedCityInseeCode: null,
            locX: null,
            locY: null,
            selectedAddress: null
        };
    },
    mounted() {
        this.fetchRegions();

        const { cookies } = useCookies();
        const cookieLoc = cookies.get("location-selected");
        console.log("cookieLoc: "+JSON.stringify(cookieLoc));
        if (cookieLoc!==null) {
            // reload location from cookie
            this.selectedRegion = cookieLoc.regionId;
            this.fetchCom2cos();
            this.selectedCom2co = cookieLoc.com2coId;
            this.selectedCom2coName = cookieLoc.com2coName;
            this.fetchCities();
            this.selectedCity = cookieLoc.cityId;
            this.selectedCityName=cookieLoc.cityName;
            this.selectedCityInseeCode = cookieLoc.cityInsee;
            this.locX = cookieLoc.lonX;
            this.locY = cookieLoc.latY;

            this.$emit('update-location', cookieLoc);

        } else {
            this.selectedRegion = '9';
            this.fetchCom2cos();
            //  this.fetchCities();
            //  this.selectedCity =2878;
        }

    },
    computed: {
        selectedCityInsee() {
            return this.cities.find(city => city.id === this.selectedCityId) || {};
        },
        selectedAddressCoordinates() {
            return this.selectedAddress.coordinates;
        }
    },
    methods: {
        async fetchRegions() { // onload
            try {
                const response = await axios.get('https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/regions.json');
                this.regions = response.data;
                this.com2cos = [];
                this.selectedCom2co = null;
                this.cities = [];
                this.selectedCity = null;
            } catch (error) {
                this.regions = [];
                this.selectedRegion = null;
                console.error('Error fetching regions:', error);
            }
        },
        async fetchCom2cos() {  // call by region
            if (!this.selectedRegion) return;
            try {
                const response = await axios.get(`https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/com2cos/${this.selectedRegion}/com2cos_${this.selectedRegion}.json`);
                this.com2cos = response.data;
                this.selectedCom2co = null;
                this.cities = [];
                this.selectedCity = null;
            } catch (error) {
                this.com2cos = [];
                this.selectedCom2co = null;
                console.error('Error fetching com2cos:', error);
            }
        },
        async fetchCities(event) { // call by com2co
            if (!this.selectedCom2co) return;
            try {
                const selectedOption = event.target.options[event.target.selectedIndex];
                this.selectedCom2coName=selectedOption.text;
                const response = await axios.get(`https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/cities/com2co/cities_${this.selectedCom2co}.json`); //${this.selectedCom2co}/
                this.cities = response.data;
            } catch (error) {
                this.cities = [];
                this.selectedCity = null;
                this.selectedCityInseeCode = null;
                console.error('Error fetching cities:', error);
            }
        },
        fetchAddresses: debounce(async function (query) {
            if (!this.selectedCity && !this.selectedCityInseeCode && query.length < 3) return;
            try {
                const response = await axios.get(`https://api-adresse.data.gouv.fr/search/?citycode=${this.selectedCityInseeCode}&q=` + encodeURI(query), { timeout: 5000 });
                const geojson = response.data;
                this.addresses = geojson.features.map(feature => ({
                    id: feature.geometry.coordinates.join(', '),
                    label: feature.properties.label,
                }));
                return this.addresses;
            } catch (error) {
                console.error('Error fetching addresses:', error);
            }
        }, 250),
        handleCityChange(event) {  // call by city
            const selectedOption = event.target.options[event.target.selectedIndex];
            this.selectedCityName = selectedOption.text;
            this.selectedCityInseeCode = selectedOption.getAttribute('data-insee-code');
            this.locX = selectedOption.getAttribute('data-longitude-x');
            this.locY = selectedOption.getAttribute('data-latitude-y');
            const loc = {
                "locType": "city",
                "regionId": this.selectedRegion,
                "com2coId": this.selectedCom2co,
                "com2coName": this.selectedCom2coName,
                "cityId": this.selectedCity,
                "cityName": this.selectedCityName,
                "cityInsee": this.selectedCityInseeCode,
                "lonX": this.locX,
                "latY": this.locY
            };

            const { cookies } = useCookies();
            cookies.set("location-selected", loc, "7d");
            //console.log("handleCityChange.emit"+JSON.stringify(loc));
            this.$emit('update-location', loc);
        },
        handleLocationSelected(loc) {
           //console.log("handleLocationSelected.emit"+JSON.stringify(loc));
            this.$emit('update-location', loc);
        }

    }
};
</script>