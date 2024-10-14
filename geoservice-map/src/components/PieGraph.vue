<template>
  <div class="my-app">
    
    <!-- chart  -->
    <D3PieChart
        :config="chart_config"
        :datum="chart_data"
        :title="chart_title"
    >
    </D3PieChart>

  </div>
</template>


<script>
// https://saigesp.github.io/vue-d3-charts/#/piechart
import { D3PieChart } from 'vue-d3-charts';

export default {
  components: {
    D3PieChart,
  },

  data() {
    return {
      chart_title: 'Proportion des habitants selon les critères OMS',
      chart_data: [
        {surface: '>0 <=1', habitants: 32543, annee: '2019', barColor: '#7b7b7b'},
        {surface: '>1 <=2', habitants: 37190, annee: '2019', barColor: '#858585'},
        {surface: '>2 <=3', habitants: 28575, annee: '2019', barColor: '#8f8f8f'},
        {surface: '>3 <=4', habitants: 17950, annee: '2019', barColor: '#999999'},
        {surface: '>4 <=5', habitants: 13575, annee: '2019', barColor: '#a3a3a3'},
        {surface: '>5 <=6', habitants: 6975, annee: '2019', barColor: '#adadad'},
        {surface: '>6 <=7', habitants: 11010, annee: '2019', barColor: '#b7b7b7'},
        {surface: '>7 <=8', habitants: 4500, annee: '2019', barColor: '#c1c1c1'},
        {surface: '>8 <=9', habitants: 3956, annee: '2019', barColor: '#cbcbcb'},
        {surface: '>9 <=10', habitants: 3499, annee: '2019', barColor: '#d5d5d5'},
        {surface: '>10 <=11', habitants: 2642, annee: '2019', barColor: '#9ee88f'},
        {surface: '>11 <=12', habitants: 2262, annee: '2019', barColor: '#9ee88f'},
        {surface: '>12 <=15', habitants: 7668, annee: '2019', barColor: '#1a9900'},
        {surface: '>15 <=20', habitants: 8461, annee: '2019', barColor: '#1a9900'},
        {surface: '>20 <=25', habitants: 5989, annee: '2019', barColor: '#1a9900'},
        {surface: '>25 <=45', habitants: 13443, annee: '2019', barColor: '#1a9900'},
        {surface: '>45 <=200', habitants: 11123, annee: '2019', barColor: '#1a9900'}
      ],
      
      chart_config: {
        key: 'surface',
        value: 'habitants',
        color: { key: ['barColor'],},
        radius: {inner: 33}
      },
      count: 1
      
      }
    }
  }

</script>

<style lang="scss">
//@import '../styles';

.chart--piechart {
  .chart__line {
    fill: none;
    stroke: black;
  }
}
</style>