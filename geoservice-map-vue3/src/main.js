//import Vue from 'vue'
import { createApp } from 'vue'
//import VueMatomo from 'vue-matomo'
//import App from './App.vue'
import HeaderAsso from './components/HeaderAsso.vue'
//import AppCarte from './views/AppCarte.vue'
//import AppSurface from './views/AppSurface.vue'
import 'leaflet/dist/leaflet.css';
import router from './router';

//d3js components
//export { default as D3BarChart } from './components/barchart/d3.barchart.vue';
//export { default as D3PieChart } from './components/piechart/d3.piechart.vue';

//Vue.config.productionTip = false

//console.log(this.$route.query.test) 

//Vue.use(VueMatomo, {
//        trackerFileName: 'ppms',
//        host: 'https://autmel.piwik.pro',
//        siteId: 'f948682b-11fe-4d25-ab02-fcfe606a7397'
//});
    
/* eslint-disable no-new */
/*
new Vue({
  el: '#app',
  router: router,
  render: h => h(HeaderAsso),
});
*/


createApp(HeaderAsso).use(router).mount('#app')

//window._paq.push(['setSiteId', dest.matomoSiteId])
//window._paq.push(['trackPageView']); //To track pageview

 
 
