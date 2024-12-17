//import Vue from 'vue'
import { createApp } from 'vue'
import VueMatomo from 'vue-matomo'
//import App from './App.vue'
import HeaderAsso from './components/HeaderAsso.vue'
//import AppCarte from './views/AppCarte.vue'
//import AppSurface from './views/AppSurface.vue'
import 'leaflet/dist/leaflet.css';
import router from './router';

//d3js components
//export { default as D3BarChart } from './components/barchart/d3.barchart.vue';
//export { default as D3PieChart } from './components/piechart/d3.piechart.vue';


const app = createApp(HeaderAsso);
app.use(VueMatomo, {
    // Configure your matomo server and site by providing
    host: 'https://autmel.piwik.pro',
    siteId: 'f948682b-11fe-4d25-ab02-fcfe606a7397'
});

app.use(router);
app.mount('#app');

router.afterEach((to, from) => {
  // Log the page view to Matomo
  window._paq.push(['setCustomUrl', to.fullPath]);
  window._paq.push(['trackPageView']);
});


 
