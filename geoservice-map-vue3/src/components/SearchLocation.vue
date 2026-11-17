<template>
    <div class="grid grid-cols-1 md:grid-cols-4 gap-2 p-2 bg-gray-50 rounded-md">
        <label class="sr-only" for="selRegion">Région (saisir pour rechercher, puis choisir dans la liste)</label>
        <label class="sr-only" for="selCom2co">Intercommunalité (saisir pour rechercher, puis choisir dans la liste)</label>
        <label class="sr-only" for="selCity">Commune (saisir pour rechercher, puis choisir dans la liste)</label>
        <input
            id="selRegion"
            list="regionsList"
            v-model="regionSearch"
            @change="handleRegionSearchChange"
            placeholder="Rechercher une région..."
            title="Région : saisir pour rechercher, puis choisir dans la liste"
            class="select-field" />
        <datalist id="regionsList">
            <option v-for="region in sortedRegions" :key="region.id" :value="region.name"></option>
        </datalist>
        
        <input
            id="selCom2co"
            list="com2cosList"
            v-model="com2coSearch"
            @change="handleCom2coSearchChange"
            :disabled="!selectedRegion"
            placeholder="Rechercher une intercommunalité..."
            title="Intercommunalité : saisir pour rechercher, puis choisir dans la liste"
            class="select-field disabled:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60" />
        <datalist id="com2cosList">
            <option v-for="com2co in sortedCom2cos" :key="com2co.id" :value="com2co.name"></option>
        </datalist>
        
        <input
            id="selCity"
            list="citiesList"
            v-model="citySearch"
            @change="handleCitySearchChange"
            :disabled="!selectedCom2co"
            placeholder="Rechercher une commune..."
            title="Commune : saisir pour rechercher, puis choisir dans la liste"
            class="select-field disabled:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60" />
        <datalist id="citiesList">
            <option v-for="city in sortedCities" :key="city.id" :value="cityLabel(city)"></option>
        </datalist>
        
        <Autocomplete 
            v-model="selectedAddress" 
            :fetch-items="fetchAddresses" 
            placeholder="Aller à une adresse"
            hint="Saisir une adresse pour positionner la carte: Base Adresse Nationale (api-adresse.data.gouv.fr)"
            :displaySearchAddress="displaySearchAddress" 
            :disabled="!selectedCity"
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
            selectedRegionName: null,
            selectedCom2co: null,
            selectedCom2coName: null,
            regionSearch: '',
            com2coSearch: '',
            selectedCity: null,
            selectedCityName: null,
            selectedCityInseeCode: null,
            citySearch: '',
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
        sortedRegions() {
            return [...this.regions].sort((a, b) =>
                a.name.localeCompare(b.name, 'fr', { sensitivity: 'base' })
            );
        },
        sortedCom2cos() {
            return [...this.com2cos].sort((a, b) =>
                a.name.localeCompare(b.name, 'fr', { sensitivity: 'base' })
            );
        },
        sortedCities() {
            return [...this.cities].sort((a, b) =>
                a.name.localeCompare(b.name, 'fr', { sensitivity: 'base' })
            );
        },
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
                        this.selectedRegionName = regionOption.name;
                        this.regionSearch = regionOption.name;
                        await this.fetchCom2cos();
                    }
                } else {
                    // Reset dependent fields if no region is selected
                    this.com2cos = [];
                    this.selectedCom2co = null;
                    this.selectedCom2coName = null;
                    this.com2coSearch = '';
                    this.cities = [];
                    this.selectedCity = null;
                }
            } catch (error) {
                this.regions = [];
                this.selectedRegion = null;
                this.regionSearch = '';
                console.error('Error fetching regions:', error);
            }
        },
        async fetchCom2cos() {  // call by region
            if (!this.selectedRegion) return;
            try {
                const response = await axios.get(`https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/com2cos/${encodeURIComponent(this.selectedRegion)}/com2cos_${encodeURIComponent(this.selectedRegion)}.json`);
                this.com2cos = response.data;
                
                // Wait for the next tick to ensure com2cos are loaded
                await this.$nextTick();
                
                // Always fetch cities if we have a selectedCom2co (from localStorage or default)
                if (this.selectedCom2co) {
                    const com2coOption = this.com2cos.find(com2co => com2co.id === this.selectedCom2co);
                    if (com2coOption) {
                        // Assign the com2co name directly
                        this.selectedCom2coName = com2coOption.name;
                        this.com2coSearch = com2coOption.name;
                    }
                    // Chain the call to fetchCities regardless of whether we found the com2co
                    await this.fetchCities();
                } else {
                    // Reset dependent fields if no com2co is selected
                    this.cities = [];
                    this.selectedCity = null;
                    this.selectedCityName = null;
                    this.selectedCityInseeCode = null;
                    this.citySearch = '';
                }
            } catch (error) {
                this.com2cos = [];
                this.selectedCom2co = null;
                this.selectedCom2coName = null;
                this.com2coSearch = '';
                console.error('Error fetching com2cos:', error);
            }
        },
        
        async fetchCities() { // call by com2co
            if (!this.selectedCom2co) return;
            try {
                const response = await axios.get(`https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/cities/com2co/cities_${encodeURIComponent(this.selectedCom2co)}.json`);
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
                        this.selectedCityName = this.cityLabel(cityOption);
                        this.selectedCityInseeCode = cityOption.inseeCode;
                        this.locX = cityOption.lonX;
                        this.locY = cityOption.latY;
                        this.citySearch = this.selectedCityName;
                        
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
                    } else {
                        // Commune mémorisée absente du nouvel EPCI : on repart sans commune.
                        this.selectedCity = null;
                        this.selectedCityName = null;
                        this.selectedCityInseeCode = null;
                        this.citySearch = '';
                    }
                }
            } catch (error) {
                this.cities = [];
                this.selectedCity = null;
                this.selectedCityName = null;
                this.selectedCityInseeCode = null;
                this.citySearch = '';
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
        handleRegionSearchChange(event) {
            const target = this.normalize(event.target.value);
            const region = this.sortedRegions.find(r => this.normalize(r.name) === target);
            if (!region) {
                // Saisie libre : on restaure la région réellement sélectionnée.
                this.regionSearch = this.selectedRegionName || '';
                return;
            }
            this.selectedRegion = region.id;
            this.selectedRegionName = region.name;
            this.regionSearch = region.name;
            // Changement de région : EPCI et commune précédents ne sont plus valides.
            this.com2cos = [];
            this.selectedCom2co = null;
            this.selectedCom2coName = null;
            this.com2coSearch = '';
            this.cities = [];
            this.selectedCity = null;
            this.selectedCityName = null;
            this.selectedCityInseeCode = null;
            this.citySearch = '';
            this.fetchCom2cos();
        },
        handleCom2coSearchChange(event) {
            const target = this.normalize(event.target.value);
            const com2co = this.sortedCom2cos.find(c => this.normalize(c.name) === target);
            if (!com2co) {
                // Saisie libre : on restaure l'EPCI réellement sélectionné.
                this.com2coSearch = this.selectedCom2coName || '';
                return;
            }
            this.selectCom2co(com2co);
        },
        selectCom2co(com2co) {  // call by com2co - direct user selection
            this.selectedCom2co = com2co.id;
            this.selectedCom2coName = com2co.name;
            this.com2coSearch = com2co.name;
            // Changement d'EPCI : la commune précédente n'est plus valide.
            this.selectedCity = null;
            this.selectedCityName = null;
            this.selectedCityInseeCode = null;
            this.citySearch = '';
            // Les écrans par EPCI (ex. NDVI) n'ont pas à attendre le choix d'une commune.
            this.$emit('com2co-selected', {
                regionId: this.selectedRegion,
                com2coId: this.selectedCom2co,
                com2coName: this.selectedCom2coName
            });
            // Sans commune choisie, l'EPCI doit quand même être retrouvé au rechargement.
            localStorage.setItem('location-selected', JSON.stringify({
                locType: 'com2co',
                regionId: this.selectedRegion,
                com2coId: this.selectedCom2co,
                com2coName: this.selectedCom2coName,
                cityId: null,
                cityName: null,
                cityInsee: null,
                lonX: null,
                latY: null
            }));
            this.fetchCities();
        },
        cityLabel(city) {
            return `${this.formatCityName(city.name)} (${city.inseeCode})`;
        },
        // CSS capitalize ne met que la 1re lettre en majuscule sans toucher au reste ("LILLE" -> "LILLE").
        formatCityName(name) {
            return String(name || '')
                .toLocaleLowerCase('fr')
                .replace(/(^|[\s\-'])(\p{L})/gu, (m, sep, letter) => sep + letter.toLocaleUpperCase('fr'));
        },
        normalize(str) {
            return String(str || '')
                .normalize('NFD')
                .replace(/[\u0300-\u036f]/g, '')
                .toLowerCase()
                .trim();
        },
        selectCity(city) {  // call by city - direct user selection
            this.selectedCity = city.id;
            this.selectedCityName = this.cityLabel(city);
            this.selectedCityInseeCode = city.inseeCode;
            this.locX = city.lonX;
            this.locY = city.latY;
            this.citySearch = this.selectedCityName;
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

            this.$emit('update-location', loc);
            // Choix explicite d'une commune : les écrans qui filtrent par commune (ex. NDVI) rappellent leurs données.
            this.$emit('city-selected', loc);
        },
        handleCitySearchChange(event) {
            const target = this.normalize(event.target.value);
            const city = this.sortedCities.find(c => this.normalize(this.cityLabel(c)) === target)
                || this.sortedCities.find(c => this.normalize(c.name) === target);
            if (city) {
                this.selectCity(city);
            } else {
                // Saisie libre : on restaure la commune réellement sélectionnée.
                this.citySearch = this.selectedCityName || '';
            }
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