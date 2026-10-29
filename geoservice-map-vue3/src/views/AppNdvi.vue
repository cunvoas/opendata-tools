<template>
    <div class="text-left align-top">
        <h1 class="text-2xl font-bold my-4">NDVI des parcs et jardins</h1>
        <p class="mb-4 max-w-3xl">
            Indice de végétation par différence normalisée (NDVI) mesuré sur chaque parc à partir
            d'images satellite Sentinel-2. Plus la valeur est élevée, plus la végétation est dense.
        </p>

        <div class="mb-4">
            <label for="ndviAnnee" class="font-medium mr-2">Année</label>
            <select id="ndviAnnee" v-model="annee" class="compact-select border rounded px-2 py-1">
                <option value="2021">2021</option>
                <option value="2019">2019</option>
                <option value="2017">2017</option>
                <option value="2015">2015</option>
            </select>
        </div>

        <div v-if="loading" role="status">Chargement en cours...</div>

        <div v-else-if="error" role="alert" class="border border-red-700 text-red-800 p-3 rounded">
            Les mesures NDVI ne sont pas disponibles pour le moment. Veuillez réessayer plus tard.
        </div>

        <div v-else-if="!data || data.withNdvi === 0" role="status" class="border p-3 rounded">
            NDVI non disponible pour cette année.
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
                        <th scope="row" class="text-left pr-2 font-normal">Dernière acquisition</th>
                        <td>{{ data.latestAcquisitionDate || 'N/A' }}</td>
                    </tr>
                </tbody>
            </table>

            <h2 class="text-xl font-bold mb-2">Répartition des parcs par NDVI moyen</h2>
            <table class="mb-8">
                <caption class="sr-only">Répartition des parcs par tranche de NDVI moyen</caption>
                <thead>
                    <tr>
                        <th scope="col" class="text-left pr-4">Tranche NDVI</th>
                        <th scope="col" class="text-left pr-4">Nombre de parcs</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="bucket in distribution" :key="bucket.label">
                        <th scope="row" class="text-left pr-4 font-normal">{{ bucket.label }}</th>
                        <td class="pr-4">{{ bucket.count }}</td>
                    </tr>
                </tbody>
            </table>

            <h2 class="text-xl font-bold mb-2">Détail par parc</h2>
            <div class="overflow-x-auto">
                <table class="w-full border-collapse">
                    <caption class="sr-only">
                        Mesures NDVI des parcs pour l'année {{ data.annee }}, triées par NDVI moyen décroissant
                    </caption>
                    <thead>
                        <tr>
                            <th scope="col" class="text-left pr-3 border-b">Parc</th>
                            <th scope="col" class="text-left pr-3 border-b">Surface (m²)</th>
                            <th scope="col" class="text-left pr-3 border-b">NDVI moyen</th>
                            <th scope="col" class="text-left pr-3 border-b">Min</th>
                            <th scope="col" class="text-left pr-3 border-b">Max</th>
                            <th scope="col" class="text-left pr-3 border-b">Écart-type</th>
                            <th scope="col" class="text-left pr-3 border-b">Acquisition</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="park in data.parks" :key="park.parkId">
                            <th scope="row" class="text-left pr-3 font-normal">{{ park.name }}</th>
                            <td class="pr-3">{{ formatNumber(park.surface, 0) }}</td>
                            <td class="pr-3">{{ formatNumber(park.ndviMean, 3) }}</td>
                            <td class="pr-3">{{ formatNumber(park.ndviMin, 3) }}</td>
                            <td class="pr-3">{{ formatNumber(park.ndviMax, 3) }}</td>
                            <td class="pr-3">{{ formatNumber(park.ndviStddev, 3) }}</td>
                            <td class="pr-3">{{ park.acquisitionDate || 'N/A' }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </template>
    </div>
</template>

<script>
const MODE = 'static';
const STATIC_ON_GIT = 'https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main';
const DYNAMIC_ON_LOCAL = 'http://localhost:8980/isochrone/geolocation';

export default {
    name: 'AppNdvi',
    data() {
        return {
            annee: '2021',
            loading: false,
            error: false,
            data: null
        };
    },
    computed: {
        coverage() {
            if (!this.data || !this.data.total) return 0;
            return Math.round((this.data.withNdvi / this.data.total) * 100);
        },
        distribution() {
            const buckets = [
                { label: 'moins de 0.2', min: -Infinity, max: 0.2 },
                { label: '0.2 à 0.3', min: 0.2, max: 0.3 },
                { label: '0.3 à 0.4', min: 0.3, max: 0.4 },
                { label: '0.4 à 0.5', min: 0.4, max: 0.5 },
                { label: '0.5 et plus', min: 0.5, max: Infinity }
            ];
            const parks = this.data?.parks || [];
            return buckets.map((bucket) => ({
                label: bucket.label,
                count: parks.filter((park) => {
                    const value = Number(park.ndviMean);
                    return value >= bucket.min && value < bucket.max;
                }).length
            }));
        }
    },
    watch: {
        annee() {
            this.load();
        }
    },
    mounted() {
        this.load();
    },
    methods: {
        getUrl() {
            if (MODE === 'dynamic') {
                return `${DYNAMIC_ON_LOCAL}/ndvi/summaries?annee=${this.annee}`;
            }
            return `${STATIC_ON_GIT}/data/ndvi/ndvi_${this.annee}.json`;
        },
        async load() {
            this.loading = true;
            this.error = false;
            this.data = null;
            try {
                const response = await fetch(this.getUrl());
                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                this.data = await response.json();
            } catch (e) {
                console.error('Erreur de chargement NDVI', e);
                this.error = true;
            } finally {
                this.loading = false;
            }
        },
        formatNumber(value, digits) {
            if (value === null || value === undefined) return 'N/A';
            return new Intl.NumberFormat('fr-FR', {
                minimumFractionDigits: digits,
                maximumFractionDigits: digits
            }).format(value);
        }
    }
};
</script>
