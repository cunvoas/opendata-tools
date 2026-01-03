/* global window, URLSearchParams */

/**
 * Utilitaire pour gérer les paramètres GET de l'URL
 * Lit les paramètres: lat, lng, region, com2co, city, annee
 * 
 * @returns {Object|null} Objet contenant les paramètres URL ou null si aucun paramètre lat/lng
 */
export function getLocationFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    
    const lat = urlParams.get('lat');
    const lng = urlParams.get('lng');
    const region = urlParams.get('region');
    const com2co = urlParams.get('com2co');
    const city = urlParams.get('city');
    const annee = urlParams.get('annee');
    
    // Retourne null si pas de coordonnées dans l'URL
    if (!lat || !lng) {
        return null;
    }
    
    // Construction de l'objet location
    const location = {
        latY: parseFloat(lat),
        lonX: parseFloat(lng),
        locType: city ? 'city' : 'address',
        fromUrl: true // Flag pour identifier que ça vient de l'URL
    };
    
    // Ajout des paramètres optionnels s'ils existent
    if (region) {
        location.regionId = region;
    }
    
    if (com2co) {
        location.com2coId = com2co;
    }
    
    if (city) {
        location.cityId = city;
    }
    
    if (annee) {
        location.annee = annee;
    }
    
    return location;
}

/**
 * Vérifie si l'URL contient des paramètres de localisation
 * 
 * @returns {boolean} true si l'URL contient lat et lng
 */
export function hasUrlLocationParams() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.has('lat') && urlParams.has('lng');
}

/**
 * Génère une URL avec les paramètres de localisation
 * 
 * @param {Object} location - Objet location contenant latY, lonX, regionId, com2coId, cityId, annee
 * @param {string} baseUrl - URL de base (par défaut: chemin actuel)
 * @returns {string} URL complète avec paramètres
 */
export function generateShareableUrl(location, baseUrl = window.location.pathname) {
    const params = new URLSearchParams();
    
    if (location.latY && location.lonX) {
        params.append('lat', location.latY.toString());
        params.append('lng', location.lonX.toString());
    }
    
    if (location.regionId) {
        params.append('region', location.regionId);
    }
    
    if (location.com2coId) {
        params.append('com2co', location.com2coId);
    }
    
    if (location.cityId) {
        params.append('city', location.cityId);
    }
    
    if (location.annee) {
        params.append('annee', location.annee);
    }
    
    const queryString = params.toString();
    return queryString ? `${baseUrl}?${queryString}` : baseUrl;
}
