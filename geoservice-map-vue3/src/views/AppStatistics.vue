<template>
    <div class="text-left align-top">
        <template v-if="territoire">
            <h1 class="text-3xl font-bold mb-4">{{ titreTerritoire }}</h1>
            <p class="mb-4">
                <template v-if="commune">
                    Surface de parcs et jardins publics accessibles à pied par habitant à {{ commune.nom }},
                    et répartition de la population par tranche, comparée aux seuils de référence repris par l'OMS.
                </template>
                <template v-else>
                    Répartition des habitants de {{ epci.nom }} par surface de parcs et jardins publics
                    accessibles à pied par habitant, selon les seuils de référence repris par l'OMS.
                </template>
                <router-link to="/information" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">Méthode détaillée</router-link>
            </p>
            <div class="border-2 border-transparent">
                <div class="text-center relative z-10">
                    <StatsGraph :key="$route.fullPath" :location="locationAffichee" />
                </div>
            </div>
            <p class="mt-4">
                <router-link to="/stats" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">Toutes les communes et intercommunalités couvertes</router-link>
            </p>
        </template>

        <template v-else>
            <h1 class="text-3xl font-bold mb-4">Statistiques des espaces verts par commune et intercommunalité</h1>
            <p v-if="inconnu" class="mb-4">
                Ce territoire n'est pas couvert par l'étude. Choisissez une commune ou une intercommunalité dans la liste ci-dessous.
            </p>
            <SearchLocation @update-location="updateLocation" @location-selected="updateLocation" :displaySearchAddress="false" />

            <Com2coQuickAccess
                :currentLocation="location"
                @graph-type-selected="updateLocation"
            />

            <div class="border-2 border-transparent">
                <div class="text-center relative z-10">
                    <StatsGraph
                        v-if="location"
                        :key="componentKey"
                        :location="location"
                    />
                </div>
            </div>

            <section aria-labelledby="territoires" class="mt-8">
                <h2 id="territoires" class="text-2xl font-bold mb-3">Territoires couverts</h2>
                <h3 class="text-lg font-semibold mb-2">Communes</h3>
                <ul class="list-disc list-inside columns-1 sm:columns-2 lg:columns-3 mb-4">
                    <li v-for="item in communes" :key="item.insee">
                        <router-link :to="urlCommune(item.insee)" class="text-blue-900 hover:text-black visited:text-blue-900 underline">{{ item.nom }}</router-link>
                    </li>
                </ul>
                <template v-if="epcis.length">
                    <h3 class="text-lg font-semibold mb-2">Intercommunalités</h3>
                    <ul class="list-disc list-inside mb-4">
                        <li v-for="item in epcis" :key="item.siren">
                            <router-link :to="urlEpci(item.siren)" class="text-blue-900 hover:text-black visited:text-blue-900 underline">{{ item.nom }}</router-link>
                        </li>
                    </ul>
                </template>
            </section>
        </template>
    </div>
</template>

<script>

import SearchLocation from "../components/SearchLocation.vue";
import StatsGraph from "../components/StatsGraph.vue";
import Com2coQuickAccess from "../components/Com2coQuickAccess.vue";
import {
    COMMUNES, EPCIS, communeParInsee, epciParSiren, urlCommune, urlEpci
} from "../utils/territoires.js";
import { dossierEpciMel } from "../utils/landingStats.js";

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
            componentKey: 0,
            communes: COMMUNES,
            epcis: EPCIS,
            urlCommune,
            urlEpci,
            inconnu: false
        };
    },
    computed: {
        // Route territoriale : /stats/<insee> ou /stats/epci/<siren>.
        commune() {
            return this.$route.name === 'statsCommune'
                ? communeParInsee(this.$route.params.insee)
                : null;
        },
        epci() {
            return this.$route.name === 'statsEpci'
                ? epciParSiren(this.$route.params.siren)
                : null;
        },
        territoire() {
            return this.commune || this.epci;
        },
        titreTerritoire() {
            if (this.commune) return `Espaces verts à ${this.commune.nom}`;
            if (this.epci) return `Espaces verts : ${this.epci.nom}`;
            return '';
        },
        /**
         * Le graphique reçoit le territoire de la route, sans passer par le localStorage :
         * l'adresse fait foi. Sur /stats, le sélecteur garde son fonctionnement historique.
         */
        locationAffichee() {
            if (this.commune) {
                return {
                    locType: 'city',
                    cityInsee: this.commune.insee,
                    cityName: this.commune.nom
                };
            }
            if (this.epci) {
                return {
                    locType: 'com2co',
                    com2coId: this.epci.dossier,
                    com2coName: this.epci.nom,
                    graphType: 'all'
                };
            }
            return this.location;
        }
    },
    watch: {
        $route: {
            immediate: true,
            handler() {
                this.inconnu = ['statsCommune', 'statsEpci'].includes(this.$route.name) && !this.territoire;
            }
        }
    },
    mounted() {
        // Sur une page territoriale, l'adresse porte le territoire : le localStorage ne décide de rien.
        if (this.territoire) return;
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
                com2coId: dossierEpciMel(),
                cityName: 'Lille',
                lat: 50.62925,
                lng: 3.057256
            };
        },
        async updateLocation(newLocation) {
            this.location = newLocation;
            this.componentKey++; // Force re-render
        }
    }
};
</script>
