<template>

  <div v-if="loaded">
    <p><b id="villeId">{{ villeNom }}</b></p>
    <p v-if="shouldDisplayBarChart">
      <Bar :data="dataBar" :options="barOptions" style="width:80%;height:300px;"  />
   </p>
    <p>
      <Pie :data="dataPie" :options="myOptions" style="width: 80%;height:450px;" />
    </p>
    <div style="width:80%;text-align:center;font-size:0.85em;margin-top:-10px;margin-bottom:20px;">
      <span>Répartition des habitants par seuil de surface de parc accessible</span>
    </div>
  </div>
  <div v-else>Chargement en cours...</div>

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

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Title, Tooltip, Legend);

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
    Pie
  },
  data() {
    return {
      dataBar:  {},
      dataPie:  {},
      annee: 2019,
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
                const percentage = ((value / total) * 100).toFixed(1);
                return `${value} habitants (${percentage}%)`;
              }
            }
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: 'Surfaces de parcs accessibles par habitant en m²'
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
                const percentage = ((value / total) * 100).toFixed(1);
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
        location: {
            async handler(newLocation) {
              
              // Accept both city and com2co location types
              if (newLocation && (newLocation.cityInsee || newLocation.locType === 'com2co')) {
                  this.loaded = false;
                  
                  await this.processLocation(toRaw(newLocation));
                  
                  /*
                  if ( newLocation instanceof Proxy) {
                    await this.processLocation ( JSON.parse(JSON.stringify(newLocation)) );
                  } else {
                    await this.processLocation(newLocation);
                  }
                    */
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
      console.log("parseJsonBar.nom:", jsonData["nom"]);
      this.villeNom = jsonData["nom"];
      
      const tLabels = jsonData["stats"].map(value => value.surface);
      // Appliquer la conversion des couleurs selon le mode daltonien
      const tFillColors = jsonData["stats"].map(value => convertToColorblindPalette(value.barColor));
      const tHabitants = jsonData["stats"].map(value => value.habitants);
 
      this.dataBar= this.getChartData(' m² par habitant de parcs', tLabels,  tHabitants, tFillColors);
    },
    parseJsonPie(jsonData) {
      const tLabels = jsonData["seuils"].map(value => value.surface);
      // Appliquer la conversion des couleurs selon le mode daltonien
      const tFillColors = jsonData["seuils"].map(value => convertToColorblindPalette(value.barColor));
      const tHabitants = jsonData["seuils"].map(value => value.habitants );  // +" ("+ value.ratio +")"

      this.dataPie= this.getChartData(' m² par habitant de parcs', tLabels,  tHabitants, tFillColors);
    },
    async processLocation(newLocation) {
      
      // Stocker la location pour pouvoir rafraîchir plus tard
      this.lastProcessedLocation = newLocation;
      
      const staticOnGit = 'https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main';
      
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

      const annee   = this.annee;

      let callUrl;
      
      // Check if we're loading com2co (communauté de communes) stats or city stats
      if (locType === 'com2co') {
        // Load community stats
        console.log("processLocation (com2co):", regionId, com2coId, graphType);
        
        // If graphType is specified, use the new URL format for specific graph types
        if (graphType) {
          // Format: https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/data/stats/com2co/1/stats_c2c_1_urbans_2019.json
          callUrl = `${staticOnGit}/data/stats/com2co/${com2coId}/stats_c2c_${com2coId}_${graphType}_${annee}.json`;
        } else {
          console.error("graphType is required for com2co location type");
          this.loaded = false;
          return;
        }
      } else {
        // Load city stats (existing behavior)
        const cityInsee = newLocation.cityInsee;
        console.log("processLocation (city):", regionId, com2coId, cityInsee);
        const dept = cityInsee.substring(0,2);
        callUrl = `${staticOnGit}/data/stats/${dept}/${cityInsee}/stats_${cityInsee}_${annee}.json`;
      }

      
      
      console.log("Fetching stats from:", callUrl);

      this.loaded = false;
      try {
        const respData = await fetch(callUrl)
        if (!respData.ok) {
            throw new Error(`HTTP error! status: ${respData.status}`);
        }   
        const newData = await respData.json();

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