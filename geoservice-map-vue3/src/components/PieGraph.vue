<template>

  <Pie :data="data" :options="myOptions" style="width: 80%;height:300px;" v-if="loaded" />
  <div v-else>Chargement en cours...</div>

</template>


<script lang="ts">


import { Chart as ChartJS, ArcElement, Tooltip , Legend} from 'chart.js';
import { Pie } from 'vue-chartjs';


ChartJS.register(ArcElement, Tooltip, Legend); //Legend




function getChartData(tLabels, tData, tFillColors) {

  const retData = {
    labels: tLabels,
    datasets: [
      {
        label: 'Habitants par m² de parc',
        data: tData,
        //backgroundColor: '#1a9900',
        backgroundColor: tFillColors, 
      }
    ],
    
  };
  return retData;
}


async function getJsonData(the_url) {
  const myStats = await fetch(the_url)
  return await myStats.json();
  };

const myUrl = "https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/test.json";


var jsonResp =   await getJsonData(myUrl);
//const tLabels = [...new Set(jsonResp["stats"].map(value => value.surface))];
const tLabels = jsonResp["seuils"].map(value => value.surface);
const tFillColors = jsonResp["seuils"].map(value => value.barColor);
const tHabitants = jsonResp["seuils"].map(value => value.habitants );  // +" ("+ value.ratio +")"


export default {
  name: 'PieGraph',
  components: {
    Pie
  },
  data() {
    return {
      data:  {},
      loaded: false,
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

  created() {
    this.fillData();
  },
 mounted() {
    //this.renderChart(this.chartData, this.options);
   this.fillData();
 },
  watch() {
    this.fillData();
  },
  methods: {
    fillData() {
      this.data= getChartData(tLabels,  tHabitants, tFillColors);
      this.loaded = true;
    }
  }
};




</script>