<template>
    <div class="container mx-auto px-4 py-6 relative">
        <a class="skip-link" href="#contenu">Aller au contenu principal</a>
        <div
            class="absolute left-4 top-2 text-xs text-gray-600 select-none"
            :title="'Version: ' + appVersion">
            v{{ appVersion }}
        </div>
        <!-- Header Title -->
        <header>
        <div class="text-center mb-6">
            <p class="text-2xl font-bold text-gray-800" @dblclick="generateShareLink" style="cursor: pointer;">Parcs accessibles en m² par habitant</p>
        </div>
        
        <!-- Logos Row -->
        <div class="flex justify-center items-center gap-10 mb-4">
            <a
                href="https://www.helloasso.com/associations/aut-mel"
                target="_blank" 
                rel="noopener noreferrer"
                class="hover:opacity-80 transition-opacity">
                <img
                    alt="Aut'MEL'"
                    src="../assets/logo-autmel.png"
                    class="h-20 border-2 border-transparent"
                />
            </a>
            <a
                href="https://www.helloasso.com/associations/deul-air"
                target="_blank" 
                rel="noopener noreferrer"
                class="hover:opacity-80 transition-opacity">
                <img
                    alt="Deûl'Air"
                    src="../assets/logo-deulair.png"
                    class="h-20 border-2 border-transparent"
                />
            </a>
            <a
                href="https://www.helloasso.com/associations/entrelianes"
                target="_blank" 
                rel="noopener noreferrer"
                class="hover:opacity-80 transition-opacity">
                <img
                    alt="Entrelianes"
                    src="../assets/logo-entrelianes.png"
                    class="w-24 border-2 border-transparent"
                />
            </a>
            <a
                href="https://www.helloasso.com/associations/lm-oxygene"
                target="_blank" 
                rel="noopener noreferrer"
                class="hover:opacity-80 transition-opacity">
                <img
                    alt="LM Oxygène"
                    src="../assets/logo-lmo.png"
                    class="h-20 border-2 border-transparent"
                />
            </a>
        </div>
        
        <!-- Navigation Links -->
        <nav class="flex flex-wrap gap-2 items-center justify-center mb-6" aria-label="Navigation principale">
            <router-link 
                to="/carte" 
                class="px-4 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition-colors font-medium no-underline"
                active-class="bg-yellow-50 text-black font-bold">
                Carte
            </router-link>
            <router-link 
                to="/stats" 
                class="px-4 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition-colors font-medium no-underline"
                active-class="bg-yellow-50 text-black font-bold">
                Statistiques
            </router-link>
            <router-link 
                to="/Information" 
                class="px-4 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition-colors font-medium no-underline"
                active-class="bg-yellow-50 text-black font-bold">
                Information
            </router-link>
            <router-link 
                to="/aideoVideo" 
                class="px-4 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition-colors font-medium no-underline"
                active-class="bg-yellow-50 text-black font-bold">
                Aide en vidéo
            </router-link>
        </nav>
        
        </header>

        <!-- Router View -->
        <main id="contenu" tabindex="-1" class="w-full">
            <router-view></router-view>
        </main>

        <footer class="mt-6 pt-4 border-t border-gray-200">
            <nav aria-label="Plan du site" class="flex flex-wrap gap-2 items-center justify-center text-sm">
                <router-link
                    to="/carte"
                    class="px-2 py-1 rounded-md text-gray-600 hover:bg-gray-100 transition-colors font-medium no-underline underline-hover"
                    active-class="bg-yellow-50 text-black font-bold">
                    Carte
                </router-link>
                <router-link
                    to="/stats"
                    class="px-2 py-1 rounded-md text-gray-600 hover:bg-gray-100 transition-colors font-medium no-underline underline-hover"
                    active-class="bg-yellow-50 text-black font-bold">
                    Statistiques
                </router-link>
                <router-link
                    to="/Information"
                    class="px-2 py-1 rounded-md text-gray-600 hover:bg-gray-100 transition-colors font-medium no-underline underline-hover"
                    active-class="bg-yellow-50 text-black font-bold">
                    Information
                </router-link>
                <router-link
                    to="/aideoVideo"
                    class="px-2 py-1 rounded-md text-gray-600 hover:bg-gray-100 transition-colors font-medium no-underline underline-hover"
                    active-class="bg-yellow-50 text-black font-bold">
                    Aide en vidéo
                </router-link>
            </nav>
        </footer>
    </div>
</template>

<script>
import { buildShareableUrl } from '../utils/urlParams.js';

export default {
  name: 'HeaderAsso',
  data() {
    return {
      appVersion: '#not-set'
    };
  },
  mounted() {
    this.loadAppVersion();
  },
  methods: {
    async loadAppVersion() {
      if (import.meta.env.DEV) {
        this.appVersion = 'vX.Y.Z_YYYYMMDD-HHmmSS';
        return;
      }

      // Fetch the version from localStorage
      const storedVersion = localStorage.getItem('app-version');
      if (storedVersion) {
        this.appVersion = storedVersion;
        return;
      }
    },
    generateShareLink() {
      // Get location data from localStorage
      const savedLocation = localStorage.getItem('location-selected');
      if (!savedLocation) {
        console.log('Aucune localisation sélectionnée');
        return;
      }
      
      try {
        const locationData = JSON.parse(savedLocation);
        const relativeUrl = buildShareableUrl(locationData);
        
        // Build full URL with FQDN and force /carte route
        const fqdn = `${window.location.protocol}//${window.location.host}`;
        
        // Extract base path (e.g., /parcs-et-jardins/) from current URL
        const pathname = window.location.pathname;
        const pathSegments = pathname.split('/').filter(Boolean);
        
        // If path has segments, keep the base (first segment) and append /carte
        const basePath = pathSegments.length > 0 ? `/${pathSegments[0]}/carte` : '/carte';
        
        // Extract query string from relativeUrl
        const queryString = relativeUrl.includes('?') ? relativeUrl.split('?')[1] : '';
        const shareableUrl = `${fqdn}${basePath}${queryString ? '?' + queryString : ''}`;

        
        // Copy the URL to clipboard
        navigator.clipboard.writeText(shareableUrl).then(() => {
          console.log('Lien copié dans le presse-papiers:', shareableUrl);
        }).catch(err => {
          console.error('Erreur lors de la copie:', err);
        });
      } catch (e) {
        console.error('Erreur lors de la génération du lien:', e);
      }
    }
  }
};
</script>

<style scoped>
.no-underline {
  text-decoration: none;
}

.skip-link {
  position: absolute;
  left: -9999px;
  top: 0;
  z-index: 10000;
  padding: 0.5rem 1rem;
  background: #ffffff;
  color: #111827;
  border: 2px solid #1d4ed8;
  border-radius: 0 0 0.375rem 0;
  font-weight: 600;
}

.skip-link:focus {
  left: 0;
  top: 0;
}
</style>