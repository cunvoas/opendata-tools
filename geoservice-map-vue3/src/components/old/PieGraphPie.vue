<template>

  <Pie :data="data" :options="myOptions" style="width: 80%;height:300px;" v-if="loaded" />
  <div v-else>Chargement en cours...</div>

</template>


<script lang="ts">


import { Chart as ChartJS, ArcElement, Tooltip } from 'chart.js';
import { Pie } from 'vue-chartjs';


ChartJS.register(ArcElement, Tooltip); //Legend




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


const jsonResp =   await getJsonData(myUrl);
//const tLabels = [...new Set(jsonResp["stats"].map(value => value.surface))];
const tLabels = jsonResp["stats"].map(value => value.surface);
const tFillColors = jsonResp["stats"].map(value => value.barColor);
const tHabitants = jsonResp["stats"].map(value => value.habitants);


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

    legend: {
      display: false,
    },
    plugins: {
      datalabels: {
        display: true,
        formatter: (val, ctx) => {
          // Grab the label for this value
          const label = ctx.chart.data.labels[ctx.dataIndex];

          // Format the number with 2 decimal places
          const formattedVal = Intl.NumberFormat('en-US', {
            minimumFractionDigits: 2,
          }).format(val);

          // Put them together
          return `${label}: ${formattedVal}`;
        },
        color: '#fff',
        backgroundColor: '#404040',
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