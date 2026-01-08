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
        console.log('🔄 SearchLocation mounted() - Début');
        
        // Try to load saved location from localStorage
        const savedLocation = localStorage.getItem('location-selected');
         console.log("savedLocation="+savedLocation);
        let locationData = null;
        if (savedLocation && savedLocation !== '{}') {
            this.fromLocalStorage = true;
            locationData = JSON.parse(savedLocation);
            console.log('📍 Localisation depuis localStorage:', locationData);
            
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
            console.log('🏠 Valeurs par défaut assignées (Lille):', {
                selectedRegion: this.selectedRegion,
                selectedCom2co: this.selectedCom2co,
                selectedCity: this.selectedCity
            });
        }

        // Chain the fetch operations with proper event simulation
        console.log('🔗 Début du chaînage des fetch operations');
        await this.fetchRegions();
        console.log('✅ fetchRegions() terminé');
        
        // Simulate region selection event
        console.log('🎭 Simulation de l\'événement région');
        const regionEvent = {
            target: {
                options: [{
                    text: this.regions.find(r => r.id === this.selectedRegion)?.name || ''
                }],
                selectedIndex: 0
            }
        };
        await this.fetchCom2cos(regionEvent);
        console.log('✅ fetchCom2cos() terminé');

        // Simulate com2co selection event
        console.log('🎭 Simulation de l\'événement com2co');
        const com2coEvent = {
            target: {
                options: [{
                    text: this.com2cos.find(c => c.id === this.selectedCom2co)?.name || ''
                }],
                selectedIndex: 0
            }
        };
        await this.fetchCities(com2coEvent);
        console.log('✅ fetchCities() terminé');

        // Emit the loaded location if we have complete data
        if (locationData && this.selectedCity) {
            // Make sure we have all required data before emitting
            const completeLocation = {
                "locType": locationData.locType || "city",
                "regionId": this.selectedRegion,
                "com2coId": this.selectedCom2co,
                "com2coName": this.selectedCom2coName,
                "cityId": this.selectedCity,
                "cityName": this.selectedCityName,
                "cityInsee": this.selectedCityInseeCode,
                "lonX": this.locX,
                "latY": this.locY
            };
            console.log('📡 Émission de la localisation complète:', completeLocation);
            this.$emit('update-location', completeLocation);
        }
        
        console.log('🏁 SearchLocation mounted() - Fin. État final:', {
            selectedRegion: this.selectedRegion,
            selectedCom2co: this.selectedCom2co,
            selectedCity: this.selectedCity,
            regionsCount: this.regions.length,
            com2cosCount: this.com2cos.length,
            citiesCount: this.cities.length
        });

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
            console.log('🌍 fetchRegions() - Début. selectedRegion actuel:', this.selectedRegion);
            try {
                console.log('📡 GET request pour les régions...');
                const response = await axios.get('https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/regions.json');
                this.regions = response.data;
                
                // Wait for the next tick to ensure regions are loaded
                await this.$nextTick();
                console.log('🔄 nextTick régions terminé');
                
                // If we have a saved region, find it in the loaded data
                if (this.selectedRegion) {
                    console.log('🔍 Recherche de la région avec ID:', this.selectedRegion);
                    
                    const regionOption = this.regions.find(region => region.id === this.selectedRegion);
                    if (regionOption) {
                        console.log('✅ Région trouvée:', regionOption.name);
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
                    } else {
                        console.log('❌ Région non trouvée avec ID:', this.selectedRegion);
                    }
                } else {
                    console.log('⚠️ selectedRegion vide, reset des champs dépendants');
                    // Reset dependent fields if no region is selected
                    this.com2cos = [];
                    this.selectedCom2co = null;
                    this.cities = [];
                    this.selectedCity = null;
                }
            } catch (error) {
                console.error('❌ Erreur fetchRegions:', error);
                this.regions = [];
                this.selectedRegion = null;
                console.error('Error fetching regions:', error);
            }
            console.log('🏁 fetchRegions() - Fin');
        },
        async fetchCom2cos() {  // call by region
            console.log('🏘️ fetchCom2cos() - Début. selectedRegion:', this.selectedRegion, 'selectedCom2co:', this.selectedCom2co);
            if (!this.selectedRegion) {
                console.log('⚠️ selectedRegion vide, arrêt de fetchCom2cos');
                return;
            }
            try {
                const url = `https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/com2cos/${this.selectedRegion}/com2cos_${this.selectedRegion}.json`;
                const response = await axios.get(url);
                console.log('✅ Com2cos récupérées:', response.data.length, 'com2cos');
                this.com2cos = response.data;
                
                // Wait for the next tick to ensure com2cos are loaded
                await this.$nextTick();
                console.log('🔄 nextTick com2cos terminé');
                
                // If we have a saved com2co, find it in the loaded data
                if (this.selectedCom2co) {
                    console.log('🔍 Recherche de la com2co avec ID:', this.selectedCom2co);
                    
                    const com2coOption = this.com2cos.find(com2co => com2co.id === this.selectedCom2co);
                    if (com2coOption) {
                        console.log('✅ Com2co trouvée:', com2coOption.name);
                        // Simulate change event for fetchCities
                        const event = {
                            target: {
                                options: [{
                                    text: com2coOption.name
                                }],
                                selectedIndex: 0
                            }
                        };
                        console.log('🎭 Simulation événement com2co pour fetchCities');
                        await this.fetchCities(event);
                    } else {
                        console.log('❌ Com2co non trouvée avec ID:', this.selectedCom2co);
                    }
                } else {
                    console.log('⚠️ selectedCom2co vide, reset des champs dépendants');
                    // Reset dependent fields if no com2co is selected
                    this.cities = [];
                    this.selectedCity = null;
                }
            } catch (error) {
                console.error('❌ Erreur fetchCom2cos:', error);
                this.com2cos = [];
                this.selectedCom2co = null;
                console.error('Error fetching com2cos:', error);
            }
            console.log('🏁 fetchCom2cos() - Fin');
        },
        
        async fetchCities(event) { // call by com2co
            console.log('🏙️ fetchCities() - Début. selectedCom2co:', this.selectedCom2co, 'selectedCity:', this.selectedCity);
            if (!this.selectedCom2co) {
                console.log('⚠️ selectedCom2co vide, arrêt de fetchCities');
                return;
            }
            try {
                const selectedOption = event.target.options[event.target.selectedIndex];
                this.selectedCom2coName = selectedOption.text;
                console.log('📝 selectedCom2coName assigné:', this.selectedCom2coName);
                
                const url = `https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/cities/com2co/cities_${this.selectedCom2co}.json`;
                console.log('📡 GET request pour les villes:', url);
                const response = await axios.get(url);
                console.log('✅ Villes récupérées:', response.data.length, 'villes');
                this.cities = response.data;
                
                // Si nous avons un selectedCity sauvegardé, attendons que les données soient chargées
                await this.$nextTick();
                console.log('🔄 nextTick cities terminé');
                
                // Si nous sommes dans le contexte du chargement initial
                if (this.selectedCity) {
                    console.log('🔍 Recherche de la ville avec ID:', this.selectedCity);
                    
                    const cityOption = this.cities.find(city => city.id === this.selectedCity);
                    if (cityOption) {
                        console.log('✅ Ville trouvée:', cityOption.name, '(' + cityOption.inseeCode + ')');
                        // Simuler l'événement change pour déclencher handleCityChange
                        const event = {
                            target: {
                                options: [{
                                    text: `${cityOption.name} (${cityOption.inseeCode})`,
                                    getAttribute: (attr) => {
                                        switch(attr) {
                                            case 'data-insee-code':
                                                return cityOption.inseeCode;
                                            case 'data-longitude-x':
                                                return cityOption.lonX;
                                            case 'data-latitude-y':
                                                return cityOption.latY;
                                            default:
                                                return null;
                                        }
                                    }
                                }],
                                selectedIndex: 0
                            }
                        };
                        console.log('🎭 Simulation événement city pour handleCityChange');
                        this.handleCityChange(event);
                    } else {
                        console.log('❌ Ville non trouvée avec ID:', this.selectedCity);
                    }
                } else {
                    console.log('⚠️ selectedCity vide, pas de simulation d\'événement');
                }
            } catch (error) {
                console.error('❌ Erreur fetchCities:', error);
                this.cities = [];
                this.selectedCity = null;
                this.selectedCityInseeCode = null;
                console.error('Error fetching cities:', error);
            }
            console.log('🏁 fetchCities() - Fin');
        },
        fetchAddresses: debounce(async function (query) {
            // debounce to avoid too many requests, call at least with 2 characters and wait 350ms after last keyup
            if (!this.selectedCity && !this.selectedCityInseeCode && query.length < 3) return [];
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
        handleCityChange(event) {  // call by city
            console.log('🏠 handleCityChange() - Début');
            const selectedOption = event.target.options[event.target.selectedIndex];
            this.selectedCityName = selectedOption.text;
            this.selectedCityInseeCode = selectedOption.getAttribute('data-insee-code');
            this.locX = selectedOption.getAttribute('data-longitude-x');
            this.locY = selectedOption.getAttribute('data-latitude-y');
            
            console.log('📝 Données de ville assignées:', {
                selectedCityName: this.selectedCityName,
                selectedCityInseeCode: this.selectedCityInseeCode,
                locX: this.locX,
                locY: this.locY
            });
            
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

            console.log('💾 Sauvegarde dans localStorage:', loc);
            // Save to localStorage instead of cookies
            localStorage.setItem('location-selected', JSON.stringify(loc));
            
            console.log('📡 Émission update-location:', loc);
            //console.log("handleCityChange.emit"+JSON.stringify(loc));
            this.$emit('update-location', loc);
            console.log('🏁 handleCityChange() - Fin');
        },
        handleLocationSelected(loc) {
           //console.log("handleLocationSelected.emit"+JSON.stringify(loc));
            this.$emit('update-location', loc);
        }

    }
};
</script>