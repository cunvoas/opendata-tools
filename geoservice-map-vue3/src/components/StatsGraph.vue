<template>

  <div v-if="loaded">
    <p>
      <Bar :data="dataBar" style="width:80%;height:300px;"  />
   </p>
    <p>
      <Pie :data="dataPie" :options="myOptions" style="width: 80%;height:300px;" />
   </p>
  </div>
  <div v-else>Chargement en cours...</div>

</template>


<script>
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
      loaded: false
    }
  },
    watch: {
        location: {
            async handler(newLocation) {
              
              if (newLocation) {
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
  options: {
    responsive: true,
    maintainAspectRatio: true,

    plugins: {
        legend: {
          position: 'top',
          display: false,
        },
        title: {
          display: true,
          text: 'Chart.js Doughnut Chart',
        },
      },

  },
  methods: {
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
      const tLabels = jsonData["stats"].map(value => value.surface);
      const tFillColors = jsonData["stats"].map(value => value.barColor);
      const tHabitants = jsonData["stats"].map(value => value.habitants);
 
      this.dataBar= this.getChartData('Habitants par m² de parc', tLabels,  tHabitants, tFillColors);
    },
    parseJsonPie(jsonData) {
      const tLabels = jsonData["seuils"].map(value => value.surface);
      const tFillColors = jsonData["seuils"].map(value => value.barColor);
      const tHabitants = jsonData["seuils"].map(value => value.habitants );  // +" ("+ value.ratio +")"

      this.dataPie= this.getChartData('Habitants par m² de parc', tLabels,  tHabitants, tFillColors);
    },
    async processLocation(newLocation) {
      

      //this.location = newLocation;

      const regionId = this.location.regionId;
      const com2coId = this.location.com2coId;
      const cityId   = this.location.cityId;

      let callUrl=""
      
     // const baseCom2co = "https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/stats/"+ this.annee +"/"+ regionId +"/com2co/" +  com2coId + "/stats_c2c__" +  this.annee + "_" +  com2coId + ".json";
     // const baseCity = "https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/stats/"+ this.annee +"/"+ regionId +"/commune/" +  cityId + "/stats_com_" +  this.annee + "_" +  cityId + ".json";
      const baseTest= "https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/stats/test.json";

      callUrl = baseTest;
      

      this.loaded = false;
      const respData = await fetch(callUrl)
      if (!respData.ok) {
          
          throw new Error(`HTTP error! status: ${respData.status}`);
      }   
      const newData = await respData.json();

      this.parseJsonBar(newData);
      this.parseJsonPie(newData);
      this.loaded = true;
    }

  }
};



</script>