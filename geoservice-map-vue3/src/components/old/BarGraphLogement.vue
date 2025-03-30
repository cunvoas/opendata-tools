<template>
  <div class="my-app">
    
    <!-- chart 
    <D3BarChart
      :config="chart_config"
      :datum="chart_data"
      :title="chart_title"
    ></D3BarChart>
    -->

  </div>
</template>

<!--
<script>
// https://saigesp.github.io/vue-d3-charts/#/barchart

import { D3BarChart } from 'vue-d3-charts';

export default {
  components: {
    D3BarChart,
  },
  data() {
    return {
      chart_title: "Type de logement sociaux construits comparés aux besoins",
      chart_data: [
        {Besoin: 77, Construction: 18, type: 'PLA-I'},
        {Besoin: 19, Construction: 56, type: 'PLUS'},
        {Besoin: 3, Construction: 17, type: 'PLS'},
        {Besoin: 1, Construction: 9, type: 'PLI'},
      ],
      chart_config: {
        key: 'type',
        values: ['Besoin', 'Construction'],
        axis: {
          yTicks: 4
        },
        color: {
          keys: {
            Besoin: 'blue',
            Construction: 'red',
          },
        },
      },
    };
  },
};

</script>
-->