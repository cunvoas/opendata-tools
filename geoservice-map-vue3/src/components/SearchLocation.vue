<template>
    <div class="grid grid-cols-1 md:grid-cols-4 gap-2 p-2 bg-gray-50 rounded-md">
        <select 
            v-model="selectedRegion" 
            @change="fetchCom2cos" 
            tabindex="1" 
            class="select-field">
            <option v-for="region in regions" :key="region.id" :value="region.id">
                {{ region.name }}
            </option>
        </select>
        
        <select 
            v-model="selectedCom2co" 
            @change="fetchCities" 
            :disabled="!selectedRegion" 
            tabindex="2" 
            class="select-field disabled:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60">
            <option v-for="com2co in com2cos" :key="com2co.id" :value="com2co.id">
                {{ com2co.name }}
            </option>
        </select>
        
        <select 
            v-model="selectedCity" 
            @change="handleCityChange" 
            :disabled="!selectedCom2co" 
            tabindex="3" 
            class="select-field disabled:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60">
            <option v-for="city in cities" :key="city.id" :value="city.id" :data-insee-code="city.inseeCode" :data-longitude-x="city.lonX" :data-latitude-y="city.latY">
                {{ city.name }} ({{ city.inseeCode }})
            </option>
        </select>
        
        <Autocomplete 
            v-model="selectedAddress" 
            :fetch-items="fetchAddresses" 
            placeholder="Aller à une adresse"
            :displaySearchAddress="displaySearchAddress" 
            :disabled="!selectedCity" 
            tabindex="4" 
            @location-selected="handleLocationSelected" 
            class="w-full" />
    </div>
</template>

<script>
import axios from 'axios';
import debounce from 'lodash/debounce';
import Autocomplete from './Autocomplete.vue';

export default {
    props: {
        displaySearchAddress: {
        type: Boolean,
        default: true
        }
    },
    components: { Autocomplete },
    data() {
        return {
            fromLocalStorage: false,
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
    async mounted() {
        
        // Try to load saved location from localStorage
        const savedLocation = localStorage.getItem('location-selected');
        let locationData = null;
        if (savedLocation && savedLocation !== '{}') {
                    this.fromLocalStorage = true;
            locationData = JSON.parse(savedLocation);
            
            // Set the region and fetch related data
            this.selectedRegion = locationData.regionId;
            // Set the com2co and fetch related data
            this.selectedCom2co = locationData.com2coId;
            // Set the city data
            this.selectedCity = locationData.cityId;
            this.selectedCityName = locationData.cityName;
            this.selectedCityInseeCode = locationData.cityInsee;
            this.locX = locationData.lonX;
            this.locY = locationData.latY;
        } else {
            // Default to Lille if no saved location
            this.selectedRegion = '9';
            this.selectedCom2co = '1';
            this.selectedCity = '2878';
        }

        // Chain the fetch operations
        await this.fetchRegions();
        
        // Fetch com2cos data (this will also call fetchCities if needed)
        await this.fetchCom2cos();

        // Emit the loaded location if we have complete data
        if (locationData) {
            this.$emit('update-location', locationData);
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
        async fetchRegions() {
            try {
                const response = await axios.get('https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/regions.json');
                this.regions = response.data;
                
                // Wait for the next tick to ensure regions are loaded
                await this.$nextTick();
                
                // If we have a saved region, find it in the loaded data
                if (this.selectedRegion) {
                    
                    const regionOption = this.regions.find(region => region.id === this.selectedRegion);
                    if (regionOption) {
                        // Simulate change event for fetchCom2cos
                        const event = {
                            target: {
                                options: [{
                                    text: regionOption.name
                                }],
                                selectedIndex: 0
                            }
                        };
                        await this.fetchCom2cos(event);
                    }
                } else {
                    // Reset dependent fields if no region is selected
                    this.com2cos = [];
                    this.selectedCom2co = null;
                    this.cities = [];
                    this.selectedCity = null;
                }
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
                
                // Wait for the next tick to ensure com2cos are loaded
                await this.$nextTick();
                
                // Always fetch cities if we have a selectedCom2co (from localStorage or default)
                if (this.selectedCom2co) {
                    const com2coOption = this.com2cos.find(com2co => com2co.id === this.selectedCom2co);
                    if (com2coOption) {
                        // Assign the com2co name directly
                        this.selectedCom2coName = com2coOption.name;
                    }
                    // Chain the call to fetchCities regardless of whether we found the com2co
                    await this.fetchCities(null);
                } else {
                    // Reset dependent fields if no com2co is selected
                    this.cities = [];
                    this.selectedCity = null;
                }
            } catch (error) {
                this.com2cos = [];
                this.selectedCom2co = null;
                console.error('Error fetching com2cos:', error);
            }
        },
        
        async fetchCities(event) { // call by com2co
            if (!this.selectedCom2co) return;
            try {
                // Only process event if it's a real event (not during initial load)
                if (event && event.target && event.target.options) {
                    const selectedOption = event.target.options[event.target.selectedIndex];
                    this.selectedCom2coName = selectedOption.text;
                } else if (!this.selectedCom2coName) {
                    // If no name is set yet, find it from the loaded com2cos
                    const com2coOption = this.com2cos.find(c => c.id === this.selectedCom2co);
                    if (com2coOption) {
                        this.selectedCom2coName = com2coOption.name;
                    }
                }
                
                const response = await axios.get(`https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/cities/com2co/cities_${this.selectedCom2co}.json`);
                this.cities = response.data;
                
                // Si nous avons un selectedCity sauvegardé, attendons que les données soient chargées
                await this.$nextTick();
                
                console.log('fetchCities - selectedCity:', this.selectedCity, 'type:', typeof this.selectedCity);
                console.log('First city in data:', this.cities[0]);
                
                // Si nous sommes dans le contexte du chargement initial
                if (this.selectedCity) {
                    
                    const cityOption = this.cities.find(city => String(city.id) === String(this.selectedCity));
                    console.log('City found:', cityOption);
                    
                    if (cityOption) {
                        // Assign city data directly
                        this.selectedCityName = `${cityOption.name} (${cityOption.inseeCode})`;
                        this.selectedCityInseeCode = cityOption.inseeCode;
                        this.locX = cityOption.lonX;
                        this.locY = cityOption.latY;
                        
                        console.log('City loaded:', {
                            selectedCityInseeCode: this.selectedCityInseeCode,
                            selectedCityName: this.selectedCityName,
                            locX: this.locX,
                            locY: this.locY
                        });
                        
                        // Save to localStorage
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
                        localStorage.setItem('location-selected', JSON.stringify(loc));
                        
                        // Emit the update-location event
                        this.$emit('update-location', loc);
                        
                        // Trigger initial address API call after all city data is loaded
                        if (this.fromLocalStorage) {
                            await this.fetchAddresses('');
                            this.fromLocalStorage = false;
                        }
                    }
                }
            } catch (error) {
                this.cities = [];
                this.selectedCity = null;
                this.selectedCityInseeCode = null;
                console.error('Error fetching cities:', error);
            }
        },
        fetchAddresses: debounce(async function (query) {
            // Don't call API if query is less than 3 characters
            if (!query || query.length < 3) return [];
            // Don't call API if city is not selected
            if (!this.selectedCity && !this.selectedCityInseeCode) return [];
            
            try {
                const response = await axios.get(`https://api-adresse.data.gouv.fr/search/?citycode=${this.selectedCityInseeCode}&q=` + encodeURI(query), { timeout: 5000 });
                const geojson = response.data;
                this.addresses = geojson.features.map(feature => ({
                    id: feature.geometry.coordinates.join(', '),
                    label: feature.properties.label,
                    score: feature.properties.score
                }));
                return this.addresses;
            } catch (error) {
                console.error('Error fetching addresses:', error);
                return [];
            }
        }, 350),
        handleCityChange(event) {  // call by city - direct user selection
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

            // Save to localStorage instead of cookies
            localStorage.setItem('location-selected', JSON.stringify(loc));

            //console.log("handleCityChange.emit"+JSON.stringify(loc));
            this.$emit('update-location', loc);
        },
        handleLocationSelected(loc) {
           //console.log("handleLocationSelected.emit"+JSON.stringify(loc));
            // Enrich the location object with region and com2co information
            const enrichedLoc = {
                ...loc,
                "regionId": this.selectedRegion,
                "com2coId": this.selectedCom2co,
                "com2coName": this.selectedCom2coName,
                "cityId": this.selectedCity,
                "cityName": this.selectedCityName,
                "cityInsee": this.selectedCityInseeCode
            };
            this.$emit('update-location', enrichedLoc);
        }

    }
};
</script>