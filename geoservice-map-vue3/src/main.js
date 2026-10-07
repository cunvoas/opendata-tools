//import Vue from 'vue'
/* eslint-env browser */
import { createApp } from 'vue'
import { initMatomo } from '@certible/use-matomo'
import HeaderAsso from './components/HeaderAsso.vue'
import 'leaflet/dist/leaflet.css';
import './assets/tailwind.css'
import router from './router';
import { getUrlParams, hasGeographicalParams } from './utils/urlParams.js'
import { initVersionCheck } from './utils/versionCheck.js'

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


app.use(router);

// Initialize version check (refresh cache every 4 hours)
initVersionCheck();

app.mount('#app');

// Wait for SW to be active before loading matomo.js —
// otherwise the script tag hits the server directly → 404
const MATOMO_CONFIG = {
    host: 'https://asso.autmel.ovh/audience',
    siteId: '1',
    trackerScriptUrl: '/parcs-et-jardins/matomo.js',
    trackerUrl: '/parcs-et-jardins/matomo.php'
};
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.ready.then(() => {
        console.log('[MATOMO] SW ready, init tracker');
        initMatomo(MATOMO_CONFIG);
    });
} else {
    console.log('[MATOMO] No SW support, init tracker directly');
    initMatomo(MATOMO_CONFIG);
}

router.afterEach((to) => {
  // Update page title and description for SEO
  if (to.meta?.title) {
    document.title = to.meta.title;
  }
  if (to.meta?.description) {
    const metaDescription = document.querySelector('meta[name="description"]');
    if (metaDescription) {
      metaDescription.setAttribute('content', to.meta.description);
    }
  }
  // Log the page view to Matomo
  window._paq = window._paq || [];
  window._paq.push(['setCustomUrl', to.fullPath]);
  window._paq.push(['trackPageView']);
  console.log('[MATOMO] trackPageView', to.fullPath);
});


 
