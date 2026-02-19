/* eslint-disable no-undef */
/**
 * Utility module for handling URL parameters to preset geographical selections
 * Allows optional region/c2c/city presets via URL parameters without affecting existing functionality
 */

/**
 * Parses URL search parameters and returns geographical presets
 * Supports the following optional query parameters:
 * - region: Region ID (e.g., ?region=9)
 * - c2c: c2c/Com2co ID (e.g., ?c2c=1)
 * - com2co: Alternative parameter name for c2c (e.g., ?com2co=1)
 * - city: City data (e.g., ?city=regionId,com2coId,cityId,lat,lng,name,locType)
 * 
 * @returns {Object} Object containing the parsed parameters:
 *   - regionId: {string|null} The region ID if provided
 *   - c2cId: {string|null} The c2c/Com2co ID if provided
 *   - city: {Object|null} City data with properties: regionId, com2coId, cityId, latY, lonX, name, locType
 * 
 * @example
 * // URL: ?region=9&c2c=1
 * getUrlParams() // Returns { regionId: '9', c2cId: '1', city: null }
 * 
 * @example
 * // URL: ?city=9,1,2878,50.6349747,3.046428,Lille,city
 * getUrlParams() // Returns { regionId: null, c2cId: null, city: { regionId: '9', com2coId: '1', cityId: '2878', latY: 50.6349747, lonX: 3.046428, name: 'Lille', locType: 'city' } }
 */
export function getUrlParams() {
  const params = new URLSearchParams(window.location.search);
  
  const result = {
    regionId: null,
    c2cId: null,
    city: null
  };
  
  // Extract region parameter
  const region = params.get('region');
  if (region) {
    result.regionId = region;
  }
  
  // Extract c2c parameter - support both 'c2c' and 'com2co' parameter names
  const c2c = params.get('c2c') || params.get('com2co');
  if (c2c) {
    result.c2cId = c2c;
  }
  
  // Extract city parameter (format: regionId,com2coId,cityId,lat,lng,name,locType)
  const city = params.get('city');
  if (city) {
    const parts = city.split(',');
    if (parts.length >= 2) {
      result.city = {
        regionId: parts[0] || null,
        com2coId: parts[1] || null,
        cityId: parts[2] || null,
        latY: parseFloat(parts[3] || parts[0]),
        lonX: parseFloat(parts[4] || parts[1]),
        name: parts[5] || null,
        locType: parts[6] || 'city'
      };
      
      // Validate coordinates
      if (isNaN(result.city.latY) || isNaN(result.city.lonX)) {
        console.warn('Invalid city coordinates in URL parameters');
        result.city = null;
      }
    }
  }
  
  return result;
}

/**
 * Builds a URL with the specified geographical parameters
 * Useful for creating shareable links with preset selections
 * 
 * @param {Object} options - Configuration object
 * @param {string} [options.regionId] - The region ID to include
 * @param {string} [options.c2cId] - The c2c/Com2co ID to include
 * @param {Object} [options.city] - City data with regionId, com2coId, cityId, latY, lonX, name, locType properties
 * @param {boolean} [options.keepExisting=true] - Whether to preserve existing URL parameters
 * 
 * @returns {string} The complete URL with parameters
 * 
 * @example
 * // Create a shareable URL with region and city
 * const url = buildUrlWithParams({
 *   regionId: '9',
 *   city: { regionId: '9', com2coId: '1', cityId: '2878', latY: 50.6349747, lonX: 3.046428, name: 'Lille', locType: 'city' }
 * });
 * // Returns: "http://example.com/?region=9&city=9,1,2878,50.6349747,3.046428,Lille,city"
 */
export function buildUrlWithParams(options = {}) {
  const {
    regionId = null,
    c2cId = null,
    city = null,
    keepExisting = true
  } = options;
  
  const params = new URLSearchParams();
  
  // Preserve existing parameters if requested
  if (keepExisting) {
    const currentParams = new URLSearchParams(window.location.search);
    for (const [key, value] of currentParams) {
      // Skip geographical parameters that will be overwritten
      if (!['region', 'c2c', 'com2co', 'city'].includes(key)) {
        params.append(key, value);
      }
    }
  }
  
  // Add region parameter
  if (regionId) {
    params.set('region', regionId);
  }
  
  // Add c2c parameter
  if (c2cId) {
    params.set('c2c', c2cId);
  }
  
  // Add city parameter with all IDs and coordinates
  if (city?.latY !== undefined && city?.lonX !== undefined) {
    // Format: regionId,com2coId,cityId,lat,lng,name,locType
    const cityStr = `${city.regionId || ''},${city.com2coId || ''},${city.cityId || ''},${city.latY},${city.lonX},${city.name || ''},${city.locType || 'city'}`;
    params.set('city', cityStr);
  }
  
  const baseUrl = window.location.pathname;
  const queryString = params.toString();
  
  return queryString ? `${baseUrl}?${queryString}` : baseUrl;
}

/**
 * Checks if URL contains any geographical parameters
 * 
 * @returns {boolean} True if any region, c2c/com2co, or city parameters exist
 */
export function hasGeographicalParams() {
  const params = getUrlParams();
  return params.region !== null || params.c2c !== null || params.city !== null;
}

/**
 * Builds a shareable URL based on the current location context from localStorage
 * Takes location data and generates a complete URL with all geographical parameters
 * 
 * @param {Object} locationData - Location data object containing regionId, com2coId, cityId, latY, lonX, cityName, locType
 * @returns {string} The complete shareable URL with geographical parameters
 * 
 * @example
 * // Location data from localStorage
 * const locationData = {
 *   regionId: '9',
 *   com2coId: '1',
 *   cityId: '2878',
 *   latY: 50.6349747,
 *   lonX: 3.046428,
 *   cityName: 'Lille',
 *   locType: 'city'
 * };
 * buildShareableUrl(locationData);
 * // Returns: "http://example.com/?city=9,1,2878,50.6349747,3.046428,Lille,city"
 */
export function buildShareableUrl(locationData) {
  if (!locationData) return defaultLoc;
  
  const city = {
    regionId: locationData.regionId || null,
    com2coId: locationData.com2coId || null,
    cityId: locationData.cityId || null,
    latY: locationData.latY,
    lonX: locationData.lonX,
    name: locationData.cityName || null,
    locType: locationData.locType || 'city'
  };
  
  // Use buildUrlWithParams with the constructed city object
  return buildUrlWithParams({
    regionId: locationData.regionId,
    c2cId: locationData.com2coId,
    city: city,
    keepExisting: false
  });
}

/**
 * Removes all geographical parameters from the current URL
 * Useful for resetting to the default state
 * 
 * @returns {string} The URL without geographical parameters
 */
export function removeGeographicalParams() {
  const params = new URLSearchParams(window.location.search);
  
  // Remove geographical parameters
  params.delete('region');
  params.delete('c2c');
  params.delete('com2co');
  params.delete('city');
  
  const baseUrl = window.location.pathname;
  const queryString = params.toString();
  
  return queryString ? `${baseUrl}?${queryString}` : baseUrl;
}

const defaultLoc = {
      "locType": "city",
      "regionId": "9",
      "com2coId": "1",
      "com2coName": "Haut-de-France",
      "cityId": "2878",
      "cityName": "LILLE",
      "cityInsee": "59350",
      "lonX": "3.046986424",
      "latY": "50.631855028"
  };

/**
 * Tests if a park/resource is active during a specific year
 * Checks if the first day of the given year falls within the active date range
 * 
 * @param {number|string} annee - The year to test (e.g., 2026)
 * @param {string} dateDebut - Start date in YYYYMMDD or YYYY-MM-DD format (inclusive)
 * @param {string} dateFin - End date in YYYYMMDD or YYYY-MM-DD format (exclusive)
 * 
 * @returns {boolean} True if dateDebut <= year+0101 < dateFin, false otherwise
 * 
 * @example
 * // Active: 20260101 <= 20260101 < 20261231
 * isActive(2026, '20260101', '20261231')  // Returns: true
 * 
 * @example
 * // Active: 20260101 <= 20270101 < 20270131 (January 1st is before January 31st)
 * isActive(2027, '20260101', '20270131')  // Returns: true
 * 
 * @example
 * // Not active: 20250101 < 20260101 (year 2025 first day is before start date)
 * isActive(2025, '20260101', '20270101')  // Returns: false
 * 
 * @example
 * // Using YYYY-MM-DD format
 * isActive(2026, '2026-01-01', '2026-12-31')  // Returns: true
 */
export function isActive(annee, dateDebut, dateFin) {
  // Normalize year to string
  const anneeStr = String(annee);
  
  // Construct the date to test: January 1st of the given year
  const dateTest = `${anneeStr}0101`;
  
  // Normalize dateDebut and dateFin to YYYYMMDD format
  const normalizedDebut = dateDebut.replace(/-/g, '');
  const normalizedFin = dateFin.replace(/-/g, '');
  
  // Test if dateDebut <= dateTest < dateFin
  return normalizedDebut <= dateTest && dateTest < normalizedFin;
}