# Configuration PWA - Analyse des Parcs et Jardins

## Résumé de la configuration

L'application est maintenant configurée en tant que **Progressive Web App (PWA)**. Cela permet aux utilisateurs de :
- ✅ Installer l'application sur l'écran d'accueil
- ✅ Utiliser l'app en mode hors ligne (avec certaines limitations)
- ✅ Accélérer le chargement avec un système de cache
- ✅ Recevoir les mises à jour automatiquement

## Fichiers modifiés/créés

### 1. **package.json**
- Ajout de `vite-plugin-pwa` comme devDependency

### 2. **vite.config.js**
Configuration du plugin PWA avec :
- **Auto-update** : Les Service Workers sont mis à jour automatiquement
- **Caching Workbox** : Stratégies de cache optimisées :
  - API calls : "NetworkFirst" (7 jours de cache)
  - Tiles Leaflet : "CacheFirst" (30 jours de cache)

### 3. **index.html**
Ajout des meta tags essentiels pour PWA :
```html
<link rel="manifest" href="/parcs-et-jardins/manifest.json" />
<meta name="theme-color" content="#2d5016" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<link rel="apple-touch-icon" href="/parcs-et-jardins/icons/icon-192x192.png" />
```

### 4. **public/manifest.json**
Fichier manifeste PWA contenant :
- Métadonnées de l'application
- Références aux icônes (192x192, 512x512)
- Configuration des shortcuts
- Configuration des screenshots

### 5. **public/icons/** (dossier)
Dossier destiné à recevoir les icônes PWA (à générer)

## Icônes requises

Pour que la PWA fonctionne correctement, vous devez créer les icônes suivantes :

| Nom | Taille | Chemin |
|-----|--------|--------|
| icon-192x192.png | 192×192 | public/icons/icon-192x192.png |
| icon-512x512.png | 512×512 | public/icons/icon-512x512.png |
| icon-maskable-192x192.png | 192×192 | public/icons/icon-maskable-192x192.png |
| icon-maskable-512x512.png | 512×512 | public/icons/icon-maskable-512x512.png |
| screenshot-1.png | 540×720 | public/icons/screenshot-1.png (optionnel) |
| screenshot-2.png | 1280×720 | public/icons/screenshot-2.png (optionnel) |

### Comment générer les icônes ?

Option 1 : **Utiliser une image existante** (ex: favicon)
```bash
# Installer sharp (image processor)
npm install --save-dev sharp

# Créer un script convert-icon.js
```

Option 2 : **Utiliser un outil en ligne**
- https://www.pwabuilder.com/imageGenerator
- Uploader votre logo en 512×512
- Télécharger tous les formats générés

Option 3 : **Utiliser ImageMagick/ImageMagick**
```bash
# Si vous avez une image source image.png
convert image.png -resize 192x192 public/icons/icon-192x192.png
convert image.png -resize 512x512 public/icons/icon-512x512.png
```

## Installation et utilisation

### Construire la PWA
```bash
npm run build
npm run preview
```

### Tester la PWA
1. Ouvrir `http://localhost:4173` (ou votre URL de preview)
2. Ouvrir les DevTools (F12)
3. Aller dans l'onglet "Application"
4. Vérifier que le Service Worker est enregistré
5. Vérifier que le manifest s'affiche correctement

### Installation sur mobile
1. Ouvrir l'application dans le navigateur mobile
2. Appuyer sur le menu (⋮)
3. Sélectionner "Installer l'application" ou "Ajouter à l'écran d'accueil"

### Installation sur desktop (Chrome/Edge)
1. Ouvrir l'application
2. Cliquer sur l'icône d'installation (⬇️ dans la barre d'adresse)

## Service Worker et caching

Le plugin vite-plugin-pwa génère automatiquement un Service Worker avec les stratégies suivantes :

### NetworkFirst (API)
- Essaie d'abord le réseau
- Utilise le cache si indisponible
- Cache jusqu'à 100 entrées, 7 jours max

### CacheFirst (Tiles)
- Utilise d'abord le cache
- Récupère les nouveaux fichiers en arrière-plan
- Cache jusqu'à 500 entrées, 30 jours max

## Considérations importantes

⚠️ **Service Worker et mise à jour**
- Les changements du Service Worker ne s'appliquent que au prochain rechargement
- La config `registerType: 'autoUpdate'` force la mise à jour

⚠️ **Cache et API**
- Les appels API sont cachés 7 jours
- Pour forcer une mise à jour : Shift+Refresh ou Clear Cache

⚠️ **Données géographiques**
- Les tiles Leaflet sont cachés 30 jours (à adapter selon vos besoins)

## Modification du cache

Pour modifier les stratégies de cache, éditez `vite.config.js` :

```javascript
runtimeCaching: [
  {
    urlPattern: /^https:\/\/api\..*/i,
    handler: 'NetworkFirst',  // ou 'CacheFirst'
    options: {
      cacheName: 'api-cache',
      expiration: {
        maxEntries: 100,
        maxAgeSeconds: 60 * 60 * 24 * 7  // 7 jours
      }
    }
  }
]
```

## Ressources utiles

- [PWA Builder](https://www.pwabuilder.com/)
- [Vite PWA Documentation](https://vite-pwa-org.netlify.app/)
- [Workbox Documentation](https://developers.google.com/web/tools/workbox)
- [MDN PWA Guide](https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps)
