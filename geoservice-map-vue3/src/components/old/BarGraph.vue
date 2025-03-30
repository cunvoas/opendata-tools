<template>
  <Bar :data="data" style="width:80%;height:300px;"  v-if="loaded"/>
  <div v-else>Chargement en cours...</div>
</template>


<script lang="ts">

import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale
} from 'chart.js'
import { Bar } from 'vue-chartjs'
// import * as chartConfig from './charts/data.js'



//import * as jsonResp from './charts/statistics_2019.json'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)


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

function fetchJSON(the_url) {
  return fetch(the_url)
      .then(response => response.json())
      .catch((error) => {
          console.log(error);
      });
}
const myUrl = "https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main/stats/test.json";
/*

(async () => {
  var jsonResp = await fetchJSON(myUrl);
  console.log(jsonResp);
})();
*/

var jsonResp =   await getJsonData(myUrl);
//const tLabels = [...new Array(jsonResp["stats"].map(value => value.surface))];
const tLabels = jsonResp["stats"].map(value => value.surface);
const tFillColors = jsonResp["stats"].map(value => value.barColor);
const tHabitants = jsonResp["stats"].map(value => value.habitants);


export default {
  name: 'BarChart',
  components: {
    Bar
  },
  data() {
    return {
      data:  {},
      loaded: false,
    }
  },
  options: {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { 
        display: false,
        position: 'right',
      },
    },
  borderWidth: 1,
  },
  created() {
    this.fillData();
  },
 mounted() {
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