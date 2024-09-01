<template>
  <div class="my-app">
    
    <!-- chart -->
    <D3BarChart
      :config="chart_config"
      :datum="chart_data"
      :title="chart_title"
    ></D3BarChart>

  </div>
</template>

<script>
// https://saigesp.github.io/vue-d3-charts/#/barchart

import { D3BarChart } from 'vue-d3-charts';

/*
7c7c7c

0.0 #7b7b7b
1.0 #858585
2.0 #8f8f8f
3.0 #999999
4.0 #a3a3a3
5.0 #adadad
6.0 #b7b7b7
7.0 #c1c1c1
8.0 #cbcbcb
9.0 #d5d5d5
10.0 #dfdfdf
11.0 #e9e9e9
12.0 #f3f3f3

*/
export default {
  components: {
    D3BarChart,
  },
  data() {
    return {
      chart_title: 'Distribution des surfaces de parc par habitant',
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
        values: ['habitants'],
        axis: {
          yTicks: 6,
        },
        color: {
          key: ['barColor'],
          current: 'yellow',
          default: 'orange'
        }
      }
    }
  }
}

</script>