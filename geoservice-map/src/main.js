import Vue from 'vue'
//import VueMatomo from 'vue-matomo'
//import App from './App.vue'
import HeaderAsso from './components/HeaderAsso.vue'
//import AppCarte from './views/AppCarte.vue'
//import AppSurface from './views/AppSurface.vue'
import 'leaflet/dist/leaflet.css';
import router from './router';

Vue.config.productionTip = false

//console.log(this.$route.query.test) 

//Vue.use(VueMatomo, {
//        trackerFileName: 'ppms',
//        host: 'https://autmel.piwik.pro',
//        siteId: 'f948682b-11fe-4d25-ab02-fcfe606a7397'
//});
    
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router: router,
  render: h => h(HeaderAsso),
});

//window._paq.push(['setSiteId', dest.matomoSiteId])
//window._paq.push(['trackPageView']); //To track pageview

 
 