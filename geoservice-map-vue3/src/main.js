//import Vue from 'vue'
import { createApp } from 'vue'
import VueMatomo from 'vue-matomo'
import HeaderAsso from './components/HeaderAsso.vue'
import 'leaflet/dist/leaflet.css';
import './assets/tailwind.css'
import router from './router';
import { getUrlParams, hasGeographicalParams } from './utils/urlParams.js'

const app = createApp(HeaderAsso);


// Helper to merge location data with URL parameters
function mergeUrlParamsIntoLocation(locationData, urlParams) {
  if (urlParams.regionId) {
    locationData.regionId = urlParams.regionId;
    console.log('[GEO] Region preset from URL:', urlParams.regionId);
  }
  
  if (urlParams.c2cId) {
    locationData.com2coId = urlParams.c2cId;
    console.log('[GEO] c2c preset from URL:', urlParams.c2cId);
  }
  
  if (urlParams.city) {
    const city = urlParams.city;
    if (city.regionId) locationData.regionId = city.regionId;
    if (city.com2coId) locationData.com2coId = city.com2coId;
    if (city.cityId) locationData.cityId = city.cityId;
    locationData.locType = city.locType || 'city';
    locationData.lonX = city.lonX;
    locationData.latY = city.latY;
    locationData.cityName = city.name;
    console.log('[GEO] Location preset from URL:', city);
  }
  
  return locationData;
}

// ========================================
// Process URL parameters at startup
//  sample: ?city=9,1,2878,50.6349747,3.046428
// ========================================
function initializeFromUrl() {
  if (!hasGeographicalParams()) return;
  
  const urlParams = getUrlParams();
  const existingLocation = localStorage.getItem('location-selected');
  const locationData = existingLocation ? JSON.parse(existingLocation) : {};
  
  mergeUrlParamsIntoLocation(locationData, urlParams);
  localStorage.setItem('location-selected', JSON.stringify(locationData));
}

// Initialize presets from URL before mounting the app
initializeFromUrl();


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


 
