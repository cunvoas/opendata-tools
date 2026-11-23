<template>
  <div class="flex flex-col items-center w-full">
    <div class="mb-4">
      <label for="anneeStats" class="font-medium mr-2">Année</label>
      <select id="anneeStats" v-model.number="annee" class="border rounded px-2 py-1">
        <option v-for="year in annees" :key="year" :value="year">{{ year }}</option>
      </select>
    </div>
    <template v-if="loaded">
      <h2 class="text-center">Répartition des habitants {{ villeNom ? 'de ' + villeNom : '' }} par surface de parc accessible à pied</h2>
      <div v-if="shouldDisplayBarChart" class="w-full max-w-4xl">
        <Bar :data="dataBar" :options="barOptions" class="w-full h-[300px]" />
      </div>
      <div class="w-full max-w-xs mt-6">
        <Pie :data="dataPie" :options="myOptions" class="w-full h-[300px]" />
        <div class="text-center text-sm mt-2 mb-5">
          <span>Répartition des habitants par seuil de surface de parc et jardin public accessible</span>
        </div>
      </div>
      <!-- Alternative textuelle pour les agents IA et l'accessibilité -->
      <StatsTableAccessibility :villeNom="villeNom" :dataPie="dataPie" :loaded="loaded" />
    </template>
    <div v-else class="text-center" role="status">Chargement en cours...</div>
  </div>
</template>


<script>

console.log("StatsGraph");

import { 
    Chart as ChartJS, 
    ArcElement, 
    Title,
    Tooltip , 
    BarElement,
    CategoryScale,
    LinearScale,
    Legend} from 'chart.js';
import { Bar, Pie } from 'vue-chartjs';
import { toRaw } from 'vue'
import StatsTableAccessibility from './StatsTableAccessibility.vue';
import { formaterNom } from '../utils/territoires.js';
import { chargerStats, urlsStatsCommune, urlsStatsEpciType } from '../utils/landingStats.js';
import { libelleTranche } from '../utils/tranches.js';

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Title, Tooltip, Legend);

// Couleurs d'affichage côté front (les données n'en portent plus).
const PALETTE_5 = ['#0000e8', '#6060e8', '#b0b0e8', '#578817', '#57ee17'];
const PALETTE_3 = ['#b0b0e8', '#578817', '#57ee17'];
const palette = (nombre) => (nombre <= 3 ? PALETTE_3 : PALETTE_5).slice(0, nombre);

/** Libellé d'une tranche v2 (bornes ou niveau OMS), sinon du seuil de secours. */
function libelleTrancheOuSeuil(tranche, secours) {
  const porteUnLibelle = tranche && (
    tranche.surface || tranche.niveauOms ||
    tranche.borneMin != null || tranche.borneMax != null
  );
  return libelleTranche(porteUnLibelle ? tranche : secours ?? {});
}

// Fonction pour convertir les couleurs normales en couleurs daltonien
function convertToColorblindPalette(normalColor) {
  // Récupérer le mode daltonien depuis le localStorage
  const savedColorblindMode = localStorage.getItem('colorblindMode');
  const colorblindMode = savedColorblindMode === 'true';
  
  if (!colorblindMode) {
    return normalColor; // Retourner la couleur d'origine si mode daltonien désactivé
  }
  
  // Mapping des couleurs normales (bleu/vert) vers couleurs daltonien (orange/bleu)
  const colorMapping = {
    // Couleurs faibles (bleu) → rouge-orangé/orange/jaune
    '#0000e8': '#d73027', // Bleu foncé → Rouge-orangé foncé
    '#6060e8': '#fc8d59', // Bleu moyen → Orange clair
    '#b0b0e8': '#fee090', // Bleu clair → Jaune-orangé
    
    // Couleurs bonnes (vert) → bleu
    '#578817': '#4575b4', // Vert foncé → Bleu foncé
    '#57ee17': '#91bfdb', // Vert clair → Bleu clair
    
    // Gris (non calculé)
    '#959595': '#959595', // Gris reste gris
  };
  
  return colorMapping[normalColor] || normalColor;
}

export default {
    props: {
      location: {
        type: Object,
        required: true
      }
    },
  name: 'StatsGraph',
  components: {
    Bar,
    Pie,
    StatsTableAccessibility
  },
  data() {
    return {
      dataBar:  {},
      dataPie:  {},
      annee: 2021,
      annees: [2021, 2019, 2017, 2015],
      loaded: false,
      villeNom: '',
      locationType: null,
      currentGraphType: null,
      lastProcessedLocation: null, // Stocker la dernière location pour rafraîchir
      barOptions: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                const value = context.parsed.y;
                const dataset = context.dataset;
                const total = dataset.data.reduce((acc, val) => acc + val, 0);
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0.0';
                return `${value} habitants (${percentage}%)`;
              }
            }
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: 'Surfaces de parcs et jardins publics accessibles par habitant en m²'
            }
          },
          y: {
            title: {
              display: true,
              text: 'Nombre d\'habitants'
            }
          }
        }
      },
      myOptions: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            position: 'top',
            display: false,
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                const value = context.parsed;
                const dataset = context.dataset;
                const total = dataset.data.reduce((acc, val) => acc + val, 0);
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0.0';
                const label = context.label || '';
                return `${label}: ${value} habitants (${percentage}%)`;
              }
            }
          }
        }
      }
    }
  },
    watch: {
        annee() {
            // Même territoire, nouveau millésime : on recharge les statistiques affichées.
            if (this.lastProcessedLocation) {
                this.processLocation(this.lastProcessedLocation);
            }
        },
        location: {
            async handler(newLocation) {
              
              // Accept both city and com2co location types
              if (newLocation && (newLocation.cityInsee || newLocation.locType === 'com2co')) {
                  this.loaded = false;
                  
                  await this.processLocation(toRaw(newLocation));
                  
                }
            },
            immediate: true,
            deep: true
        }
    },
  mounted() {
    // Écouter les changements du localStorage pour rafraîchir les graphiques
    window.addEventListener('storage', this.handleStorageChange);
    
    // Écouter les événements personnalisés pour les changements dans la même page
    window.addEventListener('colorblind-mode-changed', this.handleColorblindModeChange);
  },
  beforeUnmount() {
    // Nettoyer les listeners
    window.removeEventListener('storage', this.handleStorageChange);
    window.removeEventListener('colorblind-mode-changed', this.handleColorblindModeChange);
  },
  computed: {
    shouldDisplayBarChart() {
      // Ne pas afficher le graphique en barres si c'est une communauté de communes ET que le type est 'all'
      if (this.locationType === 'com2co' && this.currentGraphType === 'all') {
        return false;
      }
      return true;
    }
  },
  methods: {
    handleStorageChange(event) {
      // Rafraîchir les graphiques si le mode daltonien a changé
      if (event.key === 'colorblindMode' && this.lastProcessedLocation) {
        console.log('StatsGraph: colorblindMode changed in localStorage, refreshing graphs');
        this.processLocation(this.lastProcessedLocation);
      }
    },
    handleColorblindModeChange() {
      // Rafraîchir les graphiques lors d'un changement dans la même page
      if (this.lastProcessedLocation) {
        console.log('StatsGraph: colorblindMode changed via event, refreshing graphs');
        this.processLocation(this.lastProcessedLocation);
      }
    },
    getChartData(sLabel, tLabels, tData, tFillColors) {
      const retData = {
        labels: tLabels,
        datasets: [
          {
            label: sLabel,
            data: tData,
            backgroundColor: tFillColors, 
          }
        ],
      };
      return retData;
    },
    parseJsonBar(jsonData) {
      this.villeNom = this.villeNom || formaterNom(jsonData.nom);

      // Le libellé vient de la tranche, ou du seuil correspondant quand la tranche n'en porte pas.
      const tranches = jsonData.stats ?? [];
      const tLabels = tranches.map((value, index) => libelleTrancheOuSeuil(value, jsonData.seuils?.[index]));
      const tFillColors = palette(tranches.length).map(convertToColorblindPalette);
      const tHabitants = tranches.map(value => value.habitants);

      this.dataBar= this.getChartData(' m² par habitant de parcs', tLabels,  tHabitants, tFillColors);
    },
    parseJsonPie(jsonData) {
      const seuils = jsonData.seuils ?? [];
      const tLabels = seuils.map(value => libelleTranche(value));
      const tFillColors = palette(seuils.length).map(convertToColorblindPalette);
      const tHabitants = seuils.map(value => value.habitants );

      this.dataPie= this.getChartData(' m² par habitant de parcs', tLabels,  tHabitants, tFillColors);
    },
    async processLocation(newLocation) {
      
      // Stocker la location pour pouvoir rafraîchir plus tard
      this.lastProcessedLocation = newLocation;

  
      // 📊 Envoyer l'événement à Matomo
      const cityName = newLocation.cityName || 'Unknown';
      window._paq = window._paq || [];
      window._paq.push(['trackEvent', 'City Stats', newLocation.locType , cityName]);
      
      // Si vous avez aussi un ID unique : 
      //window._paq.push(['setCustomVariable', 1, 'Current City', cityName, 'page']);
      



      // Debug: log the received location
      console.log("processLocation received:", JSON.stringify(newLocation));

      const regionId = newLocation.regionId;
      const com2coId = newLocation.com2coId;
      const locType = newLocation.locType || 'city'; // Default to city if not specified
      const graphType = newLocation.graphType; // 'urbans', 'suburbs', or 'all'

      // Store location type and graph type for use in computed property
      this.locationType = locType;
      this.currentGraphType = graphType;

      console.log("Extracted values - locType:", locType, "graphType:", graphType);

      // Sur une page EPCI, le nom vient de l'index (sans le suffixe de densité du fichier de statistiques).
      this.villeNom = locType === 'com2co' ? (newLocation.com2coName || '') : '';

      const annee   = this.annee;

      let callUrls;
      
      // Check if we're loading com2co (communauté de communes) stats or city stats
      if (locType === 'com2co') {
        // Load community stats
        console.log("processLocation (com2co):", regionId, com2coId, graphType);
        
        if (graphType) {
          callUrls = urlsStatsEpciType(com2coId, graphType, annee);
        } else {
          console.error("graphType is required for com2co location type");
          this.loaded = false;
          return;
        }
      } else {
        // Load city stats (existing behavior)
        const cityInsee = newLocation.cityInsee;
        console.log("processLocation (city):", regionId, com2coId, cityInsee);
        callUrls = urlsStatsCommune(cityInsee, annee);
      }

      console.log("Fetching stats from:", callUrls);

      this.loaded = false;
      try {
        const newData = await chargerStats(callUrls);
        if (!newData) {
            throw new Error('Statistiques indisponibles');
        }

        this.parseJsonBar(newData);
        this.parseJsonPie(newData);
        this.loaded = true;
      } catch (error) {
        console.error("Error loading stats:", error);
        this.loaded = false;
      }
    }

  }
};



</script>