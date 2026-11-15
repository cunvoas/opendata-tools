<template>
    <div class="text-left align-top">
        <h1 class="text-2xl font-bold my-4">NDVI des parcs et jardins</h1>
        <p class="mb-4 max-w-3xl">
            Indice de végétation par différence normalisée (NDVI) mesuré sur chaque parc à partir
            d'images satellite Sentinel-2. Plus la valeur est élevée, plus la végétation est dense.
        </p>

        <SearchLocation class="mb-4" @com2co-selected="selectEpci" @city-selected="selectCity"
            :displaySearchAddress="false" />

        <div class="mb-4">
            <label for="ndviAnnee" class="font-medium mr-2">Année</label>
            <select id="ndviAnnee" v-model="annee" class="compact-select border rounded px-2 py-1">
                <option value="2021">2021</option>
                <option value="2019">2019</option>
                <option value="2017">2017</option>
                <option value="2015">2015</option>
            </select>
        </div>

        <p v-if="cityInsee" class="mb-4">
            Commune : <strong>{{ cityName || cityInsee }}</strong>
            <button type="button" class="ml-2 underline cursor-pointer" @click="showWholeEpci">Voir toute l'intercommunalité</button>
        </p>

        <div v-if="loading" role="status">Chargement en cours...</div>

        <div v-else-if="error" role="alert" class="border border-red-700 text-red-800 p-3 rounded">
            Les mesures NDVI ne sont pas disponibles pour le moment. Veuillez réessayer plus tard.
        </div>

        <div v-else-if="!data || data.withNdvi === 0" role="status" class="border p-3 rounded">
            NDVI non disponible pour ce territoire et cette année.
        </div>

        <template v-else>
            <table class="mb-6">
                <caption class="sr-only">Couverture des mesures NDVI pour l'année {{ data.annee }}</caption>
                <tbody>
                    <tr>
                        <th scope="row" class="text-left pr-2 font-normal">Parcs calculés</th>
                        <td>{{ data.total }}</td>
                    </tr>
                    <tr>
                        <th scope="row" class="text-left pr-2 font-normal">Parcs avec NDVI</th>
                        <td>{{ data.withNdvi }} ({{ coverage }} %)</td>
                    </tr>
                    <tr>
                        <th scope="row" class="text-left pr-2 font-normal">Parcs sans NDVI</th>
                        <td>{{ withoutNdvi }} ({{ 100 - coverage }} %)</td>
                    </tr>
                    <tr>
                        <th scope="row" class="text-left pr-2 font-normal">Dernière acquisition</th>
                        <td>{{ formatDate(data.latestAcquisitionDate) }}</td>
                    </tr>
                </tbody>
            </table>

            <h2 class="text-xl font-bold mb-2">Répartition des parcs par NDVI moyen</h2>
            <div class="mb-8 max-w-3xl" role="group" aria-label="Répartition des parcs par tranche de NDVI moyen">
                <div v-for="bucket in distribution" :key="bucket.range" class="flex items-center gap-2 mb-1">
                    <span class="w-48 sm:w-96 shrink-0">{{ bucket.range }} : {{ bucket.label }}</span>
                    <span class="flex-1 bg-gray-100">
                        <span class="block h-4" :style="{ width: bucket.barWidth, backgroundColor: pastille(bucket.color) }" />
                    </span>
                    <span class="w-10 text-right">{{ bucket.count }} <span class="sr-only">{{ bucket.count > 1 ? 'parcs' : 'parc' }}</span></span>
                </div>
            </div>

            <h2 class="text-xl font-bold mb-2">Détail par parc</h2>
            <div class="mb-3 max-w-sm">
                <label for="ndviFiltreParc" class="font-medium block mb-1">Filtrer par nom de parc</label>
                <input id="ndviFiltreParc" v-model="filterText" type="search" autocomplete="off"
                    class="input-field text-sm" />
            </div>
            <p class="sr-only" role="status">{{ statusMessage }}</p>
            <div class="overflow-x-auto">
                <table class="w-full border-collapse">
                    <caption class="sr-only">
                        Mesures NDVI des parcs pour l'année {{ data.annee }}, triées par {{ sortDescription }}
                    </caption>
                    <thead>
                        <tr>
                            <th v-for="column in columns" :key="column.key" scope="col"
                                class="text-left pr-3 border-b" :aria-sort="ariaSort(column.key)">
                                <button type="button" class="text-left hover:underline cursor-pointer"
                                    @click="sortBy(column.key)">
                                    {{ column.label }}
                                    <span aria-hidden="true" class="inline-block w-4 text-gray-600">{{ sortIndicator(column.key) }}</span>
                                </button>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="park in sortedParks" :key="park.parkId">
                            <th scope="row" class="text-left pr-3 font-normal">{{ park.name }}</th>
                            <td class="pr-3">{{ formatNumber(park.surface, 0) }}</td>
                            <td class="pr-3">{{ formatNumber(park.ndviMean, 3) }}</td>
                            <td class="pr-3 whitespace-nowrap">
                                <span v-if="ndviColor(park.ndviMean)" class="inline-block w-3 h-3 mr-1 align-middle border border-gray-600"
                                    :style="{ backgroundColor: pastille(ndviColor(park.ndviMean)) }" aria-hidden="true" />{{ ndviCategory(park.ndviMean) }}
                            </td>
                            <td class="pr-3">{{ formatNumber(park.ndviMin, 3) }}</td>
                            <td class="pr-3">{{ formatNumber(park.ndviMax, 3) }}</td>
                            <td class="pr-3">{{ formatNumber(park.ndviStddev, 3) }}</td>
                            <td class="pr-3">{{ formatDate(park.acquisitionDate) }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </template>
    </div>
</template>

<script>
import SearchLocation from '../components/SearchLocation.vue';
import { NDVI_CATEGORIES, couleurCategorie, getNdviCategory } from '../utils/ndvi.js';
import { hexToRgba } from '../utils/color.js';
import { DATA_MODE, STATIC_ON_GIT, DYNAMIC_ON_LOCAL } from '../utils/config.js';

function savedLocation() {
    try {
        return JSON.parse(localStorage.getItem('location-selected') || '{}');
    } catch (e) {
        return {};
    }
}

function currentEpciId() {
    const params = new URLSearchParams(window.location.search);
    const saved = savedLocation();
    return String(params.get('com2co') || params.get('c2c') || saved.com2coId || '1');
}

function currentCityInsee() {
    return new URLSearchParams(window.location.search).get('city');
}

const COLUMNS = [
    { key: 'name', label: 'Parc', type: 'text' },
    { key: 'surface', label: 'Surface (m²)', type: 'number' },
    { key: 'ndviMean', label: 'NDVI moyen', type: 'number' },
    { key: 'ndviCategory', label: 'Catégorie NDVI', type: 'number', field: 'ndviMean' },
    { key: 'ndviMin', label: 'Min', type: 'number' },
    { key: 'ndviMax', label: 'Max', type: 'number' },
    { key: 'ndviStddev', label: 'Écart-type', type: 'number' },
    { key: 'acquisitionDate', label: 'Acquisition', type: 'date' }
];
const collator = new Intl.Collator('fr', { sensitivity: 'base', numeric: true });

// Apostrophes typographiques (claviers mobiles) ramenées à l'apostrophe droite des noms de parcs.
function normalize(text) {
    return String(text ?? '')
        .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
        .replace(/[\u2018\u2019]/g, "'")
        .toLowerCase();
}

function sortValue(park, column) {
    const value = park[column.field || column.key];
    if (value === null || value === undefined || value === '') return null;
    if (column.type === 'text') return String(value);
    const parsed = column.type === 'date' ? new Date(value).getTime() : Number(value);
    return Number.isNaN(parsed) ? null : parsed;
}

export default {
    name: 'AppNdvi',
    components: { SearchLocation },
    data() {
        return {
            annee: '2021',
            // Même préférence que la carte : le mode daltonien est mémorisé dans localStorage.
            daltonien: localStorage.getItem('colorblindMode') === 'true',
            loading: false,
            error: false,
            data: null,
            requestId: 0,
            epciId: '1',
            cityInsee: null,
            cityName: '',
            columns: COLUMNS,
            sortKey: 'ndviMean',
            sortDir: 'desc',
            filterText: '',
            statusMessage: ''
        };
    },
    computed: {
        sortColumn() {
            return COLUMNS.find((column) => column.key === this.sortKey);
        },
        sortDescription() {
            return `${this.sortColumn.label}, ordre ${this.sortDir === 'asc' ? 'croissant' : 'décroissant'}`;
        },
        filteredParks() {
            const parks = this.data?.parks || [];
            const terms = normalize(this.filterText).split(/\s+/).filter(Boolean);
            if (!terms.length) return parks;
            return parks.filter((park) => {
                const name = normalize(park.name);
                return terms.every((term) => name.includes(term));
            });
        },
        sortedParks() {
            const column = this.sortColumn;
            const factor = this.sortDir === 'asc' ? 1 : -1;
            // Les valeurs absentes restent en fin de tableau, quel que soit le sens du tri.
            return [...this.filteredParks].sort((a, b) => {
                const valueA = sortValue(a, column);
                const valueB = sortValue(b, column);
                if (valueA === null && valueB === null) return 0;
                if (valueA === null) return 1;
                if (valueB === null) return -1;
                const diff = column.type === 'text' ? collator.compare(valueA, valueB) : valueA - valueB;
                return diff * factor;
            });
        },
        coverage() {
            if (!this.data || !this.data.total) return 0;
            return Math.round((this.data.withNdvi / this.data.total) * 100);
        },
        withoutNdvi() {
            if (!this.data) return 0;
            return Math.max(0, this.data.total - this.data.withNdvi);
        },
        distribution() {
            const parks = this.data?.parks || [];
            const counts = NDVI_CATEGORIES.map((category) => ({
                ...category,
                color: couleurCategorie(category, this.daltonien),
                count: parks.filter((park) => getNdviCategory(park.ndviMean) === category).length
            }));
            const max = Math.max(1, ...counts.map((bucket) => bucket.count));
            return counts.map((bucket) => ({
                ...bucket,
                barWidth: `${Math.round((bucket.count / max) * 100)}%`
            }));
        }
    },
    watch: {
        annee() {
            this.load();
        },
        filterText() {
            if (!this.data) return;
            const count = this.filteredParks.length;
            const plural = count > 1 ? 's' : '';
            this.statusMessage = `${count} parc${plural} affiché${plural} sur ${this.data?.parks.length ?? 0}`;
        }
    },
    mounted() {
        this.epciId = currentEpciId();
        // La commune peut venir du lien (écran MVC) ou du sélecteur mémorisé, si elle appartient à cet EPCI.
        const saved = savedLocation();
        const cityParam = currentCityInsee();
        if (cityParam) {
            this.cityInsee = String(cityParam);
            this.cityName = String(saved.cityInsee) === this.cityInsee ? (saved.cityName || '') : '';
        } else if (saved.cityInsee && String(saved.com2coId) === this.epciId) {
            this.cityInsee = String(saved.cityInsee);
            this.cityName = saved.cityName || '';
        }
        this.load();
    },
    methods: {
        // Même transparence (0,5) que les polygones et la légende de la carte.
        pastille(hex) {
            return hexToRgba(hex, 0.5);
        },
        ndviCategory(value) {
            return getNdviCategory(value)?.label ?? 'N/A';
        },
        ndviColor(value) {
            const category = getNdviCategory(value);
            return category ? couleurCategorie(category, this.daltonien) : null;
        },
        selectEpci(location) {
            if (!location?.com2coId) return;
            this.changeScope(String(location.com2coId), null, '');
        },
        selectCity(location) {
            if (!location?.com2coId || !location?.cityInsee) return;
            this.changeScope(String(location.com2coId), String(location.cityInsee), location.cityName || '');
        },
        showWholeEpci() {
            this.changeScope(this.epciId, null, '');
        },
        changeScope(epciId, cityInsee, cityName) {
            if (epciId === this.epciId && cityInsee === this.cityInsee) return;
            this.epciId = epciId;
            this.cityInsee = cityInsee;
            this.cityName = cityName;
            this.filterText = '';
            this.load();
        },
        getUrl() {
            const epci = encodeURIComponent(this.epciId);
            const city = this.cityInsee === null ? null : encodeURIComponent(this.cityInsee);
            if (DATA_MODE === 'dynamic') {
                return `${DYNAMIC_ON_LOCAL}/map/ndvi/summaries?annee=${this.annee}&com2co=${epci}${city ? `&city=${city}` : ''}`;
            }
            if (city) {
                return `${STATIC_ON_GIT}/data/ndvi/${epci}/${city}/ndvi_${this.annee}_${city}.json`;
            }
            return `${STATIC_ON_GIT}/data/ndvi/${epci}/ndvi_${this.annee}_${epci}.json`;
        },
        async load() {
            this.loading = true;
            this.error = false;
            this.data = null;
            this.statusMessage = '';
            const requestId = (this.requestId || 0) + 1;
            this.requestId = requestId;
            try {
                const response = await fetch(this.getUrl());
                if (response.status === 404) return; // aucun fichier publié pour cet EPCI : « NDVI non disponible »
                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                const payload = await response.json();
                this.validate(payload);
                if (requestId !== this.requestId) return;
                this.data = payload;
            } catch (e) {
                if (requestId !== this.requestId) return;
                console.error('Erreur de chargement NDVI', e);
                this.error = true;
            } finally {
                if (requestId === this.requestId) {
                    this.loading = false;
                }
            }
        },
        validate(payload) {
            if (!payload || !Array.isArray(payload.parks)) {
                throw new Error('Réponse NDVI vide ou invalide');
            }
            const total = Number(payload.total);
            const withNdvi = Number(payload.withNdvi);
            if (!Number.isFinite(total) || !Number.isFinite(withNdvi)) {
                throw new TypeError('Réponse NDVI incomplète');
            }
        },
        sortBy(key) {
            if (this.sortKey === key) {
                this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
            } else {
                this.sortKey = key;
                this.sortDir = 'asc';
            }
            this.statusMessage = `Tableau trié par ${this.sortDescription}`;
        },
        ariaSort(key) {
            if (this.sortKey !== key) return undefined;
            return this.sortDir === 'asc' ? 'ascending' : 'descending';
        },
        sortIndicator(key) {
            if (this.sortKey !== key) return '↕';
            return this.sortDir === 'asc' ? '▲' : '▼';
        },
        formatNumber(value, digits) {
            if (value === null || value === undefined) return 'N/A';
            return new Intl.NumberFormat('fr-FR', {
                minimumFractionDigits: digits,
                maximumFractionDigits: digits
            }).format(value);
        },
        formatDate(value) {
            if (!value) return 'N/A';
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return String(value);
            return date.toLocaleDateString('fr-FR');
        }
    }
};
</script>
