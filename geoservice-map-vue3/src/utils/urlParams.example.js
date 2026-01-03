/**
 * INTEGRATION GUIDE: Complete example with main.js
 * 
 * This file shows the complete integration of URL parameter handling
 * at application startup, with all parameters stored in localStorage.
 */

// ============================================
// COMPLETE main.js IMPLEMENTATION
// ============================================

/*
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { getUrlParams, hasGeographicalParams } from './utils/urlParams.js'

const app = createApp(App)

// ========================================
// Process URL parameters at startup
// ========================================
function initializeFromUrl() {
  if (hasGeographicalParams()) {
    const urlParams = getUrlParams()
    
    // Load existing location data from localStorage
    const existingLocation = localStorage.getItem('location-selected')
    let locationData = existingLocation ? JSON.parse(existingLocation) : {}
    
    // Update location data with URL parameters
    if (urlParams.regionId) {
      locationData.regionId = urlParams.regionId
      console.log('[GEO] Region preset from URL:', urlParams.regionId)
    }
    
    if (urlParams.epciId) {
      locationData.com2coId = urlParams.epciId
      console.log('[GEO] EPCI preset from URL:', urlParams.epciId)
    }
    
    if (urlParams.city) {
      const city = urlParams.city
      locationData.locType = city.locType || 'city'
      locationData.lonX = city.lonX
      locationData.latY = city.latY
      locationData.cityName = city.name
      console.log('[GEO] Location preset from URL:', city)
    }
    
    // Save combined data to localStorage (same format as SearchLocation.vue)
    localStorage.setItem('location-selected', JSON.stringify(locationData))
  }
}

// Initialize presets from URL before mounting the app
initializeFromUrl()

app.use(router)
app.mount('#app')
*/

// ============================================
// USAGE IN Isochrone.vue COMPONENT
// ============================================

/*
Replace your current data() and created() methods with this:

data() {
  // Load geographical presets from localStorage (same format as SearchLocation.vue)
  const savedLocation = localStorage.getItem('location-selected')
  let locationData = null
  let region = '9'
  let com2co = '1'
  let center = [50.6349747, 3.046428]
  let zoom = 14
  
  if (savedLocation) {
    try {
      locationData = JSON.parse(savedLocation)
      
      // Apply region if available
      if (locationData.regionId) {
        region = locationData.regionId
      }
      
      // Apply EPCI/Com2co if available
      if (locationData.com2coId) {
        com2co = locationData.com2coId
      }
      
      // Apply location coordinates if available
      if (locationData.latY && locationData.lonX) {
        center = [locationData.latY, locationData.lonX]
        zoom = locationData.locType === 'address' ? 17 : 14
      }
    } catch (e) {
      console.error('[GEO] Error parsing location data from localStorage:', e)
    }
  }
  
  return {
    // Use preset values
    region: region,
    com2co: com2co,
    zoom: zoom,
    center: center,
    
    // ... rest of your existing data properties
    htmlLegend: null,
    leafletMap: null,
    loading: false,
    showIsochrones: false,
    showCarre: true,
    showParcs: false,
    showCadastre: false,
    colorblindMode: localStorage.getItem('colorblindMode') !== null 
      ? localStorage.getItem('colorblindMode') === 'true' 
      : false,
    refreshKey: 0,
    legendeDense: true,
    minZoom: 10,
    maxZoom: 18,
    bounds: null,
    boundSwLat: 0,
    boundSwLng: 0,
    boundNeLat: 0,
    boundNeLng: 0,
    geojsonCadastre: null,
    geojsonCarre: null,
    geojsonIsochrone: null,
    geojsonParcs: null,
    annee: "2019",
    fillColor: "#A0DCA0",
    // ... tileProviders, addressIcon, etc.
  }
},

created() {
  // Initialize legend
  this.htmlLegend = getColorLegend(true, this.colorblindMode)
  console.log('[GEO] Isochrone created - colorblindMode:', this.colorblindMode)
  
  // Load data if COM2CO preset exists
  const savedLocation = localStorage.getItem('location-selected')
  if (savedLocation) {
    try {
      const locationData = JSON.parse(savedLocation)
      
      // If we have a COM2CO ID, load the data for it
      if (locationData.com2coId && this.com2co === locationData.com2coId) {
        console.log('[GEO] Loading data for EPCI:', locationData.com2coId)
        
        // Trigger API calls for the preset EPCI
        this.callGeoJsonParcs()
        this.callGeoJsonIsochrones()
        this.callGeoJsonCarres()
        this.callGeoJsonCadastre()
      }
      
      // If we have location coordinates, fetch commune data
      if (locationData.latY && locationData.lonX) {
        console.log('[GEO] Fetching commune data for location:', locationData)
        this.debouncedFetchCommune(locationData.latY, locationData.lonX)
      }
    } catch (e) {
      console.error('[GEO] Error processing location data:', e)
    }
  }
}
*/

// ============================================
// OPTIONAL: Add reset method to Isochrone.vue
// ============================================

/*
Add this method to clear all geographical presets:

methods: {
  clearGeographicalPresets() {
    localStorage.removeItem('geoPreset_region')
    localStorage.removeItem('geoPreset_epci')
    localStorage.removeItem('geoPreset_city')
    
    console.log('[GEO] Geographical presets cleared')
    
    // Reload to show default state (optional)
    // window.location.href = window.location.pathname
  }
}

// Template usage:
// <button @click="clearGeographicalPresets" class="reset-button">Reset Geographical Presets</button>
*/

// ============================================
// URL PARAMETER EXAMPLES
// ============================================

/*
All parameters are optional and won't affect existing functionality if not provided.

EXAMPLE 1: Only region
URL: http://example.com/?region=9
Result in localStorage['location-selected']:
  {
    "regionId": "9"
  }

EXAMPLE 2: Only EPCI/Com2co
URL: http://example.com/?epci=1
Result in localStorage['location-selected']:
  {
    "com2coId": "1"
  }
  - API calls are triggered for EPCI 1
  - GeoJSON data loads automatically

EXAMPLE 3: Only location
URL: http://example.com/?city=50.6349747,3.046428,Lille,city
Result in localStorage['location-selected']:
  {
    "locType": "city",
    "lonX": 3.046428,
    "latY": 50.6349747,
    "cityName": "Lille"
  }
  - Map centers on Lille with city zoom (14)
  - Commune data is fetched

EXAMPLE 4: Location with address zoom
URL: http://example.com/?city=48.8566,2.3522,Paris,address
Result in localStorage['location-selected']:
  {
    "locType": "address",
    "lonX": 2.3522,
    "latY": 48.8566,
    "cityName": "Paris"
  }
  - Map centers on Paris with address zoom (17)
  - Commune data is fetched

EXAMPLE 5: Combined - all parameters
URL: http://example.com/?region=9&epci=1&city=50.6349747,3.046428,Lille,city
Result in localStorage['location-selected']:
  {
    "regionId": "9",
    "com2coId": "1",
    "locType": "city",
    "lonX": 3.046428,
    "latY": 50.6349747,
    "cityName": "Lille"
  }
  - Region 9 is selected
  - EPCI 1 data loads
  - Map centers on Lille with city zoom
  - All API calls are triggered

EXAMPLE 6: Alternative com2co parameter name
URL: http://example.com/?com2co=1
Result: Same as ?epci=1
  - localStorage['location-selected'].com2coId = '1'
*/

// ============================================
// localStorage FORMAT
// ============================================

/*
The localStorage uses the same format as SearchLocation.vue with a single key:

localStorage.setItem('location-selected', JSON.stringify({
  locType: 'city' or 'address',       // Type of location
  regionId: '9',                       // Region ID
  com2coId: '1',                       // EPCI/Com2co ID
  com2coName: 'Name of EPCI',          // (optional) Name of the EPCI
  cityId: '2878',                      // (optional) City ID
  cityName: 'Lille',                   // (optional) City name
  cityInsee: '59350',                  // (optional) INSEE code
  lonX: 3.046428,                      // Longitude X coordinate
  latY: 50.6349747                     // Latitude Y coordinate
}))

Note: Only the necessary fields need to be populated for URL parameters.
SearchLocation.vue will populate the rest when user makes selections.
*/

// ============================================
// HELPER FUNCTIONS FROM urlParams.js
// ============================================

/*
getUrlParams()
  Returns: Object { regionId, epciId, city }
  Parses URL query parameters and extracts geographical presets
  
buildUrlWithParams(options)
  Returns: String (URL)
  Creates shareable URL with geographical parameters
  Options: { regionId, epciId, city, keepExisting: true }
  
hasGeographicalParams()
  Returns: Boolean
  Checks if URL contains any geographical parameters
  
removeGeographicalParams()
  Returns: String (URL without geographical params)
  Useful for reset/clean URL functionality
*/

// ============================================
// FLOW DIAGRAM
// ============================================

/*
User visits: http://example.com/?region=9&epci=1&city=50.6,3.0,Lille,city
                                    ↓
                    [main.js] initializeFromUrl()
                                    ↓
                    Read URL parameters via getUrlParams()
                                    ↓
       Merge with existing localStorage['location-selected'] if it exists
                                    ↓
    Save combined location data to localStorage with format:
    { regionId, com2coId, locType, lonX, latY, cityName }
                                    ↓
                        Mount Vue app
                                    ↓
        [SearchLocation.vue] mounted() hook loads from localStorage
                                    ↓
    Populates: selectedRegion, selectedCom2co, selectedCity
    and fetches associated data (regions, com2cos, cities)
                                    ↓
        [Isochrone.vue] data() loads from localStorage
                                    ↓
    Initialize region, com2co, center, zoom with preset values
                                    ↓
            [Isochrone.vue] created() hook triggers
                                    ↓
    Fetch commune data + Load GeoJSON for preset EPCI
                                    ↓
            Map displays with all presets applied
                                    ↓
        User can interact normally - no functional changes
*/
