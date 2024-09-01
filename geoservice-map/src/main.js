import Vue from 'vue'
//import App from './App.vue'
import HeaderAsso from './components/HeaderAsso.vue'
//import AppCarte from './views/AppCarte.vue'
//import AppSurface from './views/AppSurface.vue'
import 'leaflet/dist/leaflet.css';
import router from './router';

Vue.config.productionTip = false


/* eslint-disable no-new */
new Vue({
  el: '#app',
  router: router,
  render: h => h(HeaderAsso),
});