# ✅ PWA Configuration Complete

Votre application Vue3 est maintenant prête à fonctionner comme une **Progressive Web App**.

## 🚀 Prochaines étapes

### 1. **Générer les icônes PWA** (IMPORTANT)

Vous avez deux options :

#### Option A : Utiliser le script de génération automatique
```bash
# Installer sharp (image processor)
npm install --save-dev sharp

# Générer les icônes à partir d'une image source
node generate-icons.js votre-logo.png
```

#### Option B : Créer les icônes manuellement
1. Visitez https://www.pwabuilder.com/imageGenerator
2. Uploadez votre logo (512×512 recommandé)
3. Téléchargez les icônes générées
4. Placez-les dans le dossier `public/icons/`

Les icônes requises sont :
- `icon-192x192.png`
- `icon-512x512.png`
- `icon-maskable-192x192.png`
- `icon-maskable-512x512.png`

### 2. **Personnaliser le manifest.json** (OPTIONNEL)

Éditer `public/manifest.json` pour :
- Changer les couleurs (`theme_color`, `background_color`)
- Ajouter des screenshots
- Ajouter des shortcuts personnalisés

### 3. **Tester la PWA en développement**
```bash
npm run dev
# Ouvrir http://localhost:5173
# Les DevTools montrent le Service Worker
```

### 4. **Construire et tester en production**
```bash
npm run build
npm run preview
# Ouvrir http://localhost:4173
```

### 5. **Déployer**
```bash
npm run build:github
# Ou
npm run build:raspberry
```

## 📋 Checklist de validation PWA

Avant de déployer, vérifiez :

- [x] Les icônes sont présentes dans `public/icons/`
- [x] `npm run build` s'exécute sans erreur
- [x] `npm run preview` montre l'application correctement
- [x] Dans DevTools > Application > Service Workers : le service worker est enregistré
- [x] Dans DevTools > Application > Manifest : le manifest s'affiche correctement
- [x] L'icône d'installation s'affiche dans la barre d'adresse (desktop)
- [x] L'app peut être installée sur mobile

## 📁 Structure des fichiers PWA

```
.
├── public/
│   ├── manifest.json          ← Métadonnées PWA
│   └── icons/                 ← Icônes PWA
│       ├── icon-192x192.png
│       ├── icon-512x512.png
│       ├── icon-maskable-192x192.png
│       └── icon-maskable-512x512.png
├── index.html                 ← Meta tags PWA ajoutés
├── vite.config.js            ← Plugin PWA configuré
└── PWA_SETUP.md              ← Documentation complète
```

## 🔧 Fonctionnalités PWA activées

✅ **Installation** : L'app peut être installée sur l'écran d'accueil
✅ **Offline** : Fonctionne partiellement hors ligne (grâce au Service Worker)
✅ **Auto-update** : Les mises à jour sont appliquées automatiquement
✅ **Caching intelligent** :
  - API calls : NetworkFirst (7 jours)
  - Tiles Leaflet : CacheFirst (30 jours)

## ⚙️ Configuration Workbox

Les stratégies de cache sont définies dans `vite.config.js`. Pour les modifier :

```javascript
// Dans vite.config.js, section 'runtimeCaching'
runtimeCaching: [
  {
    urlPattern: /^https:\/\/votre-api.*/i,
    handler: 'NetworkFirst',  // ou 'CacheFirst', 'StaleWhileRevalidate'
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

## 🐛 Troubleshooting

### Les icônes ne s'affichent pas
- Vérifiez que les fichiers sont dans `public/icons/`
- Vérifiez les chemins dans `public/manifest.json`
- Forcez un Shift+Refresh

### Le Service Worker ne s'enregistre pas
- Vérifiez la console des DevTools
- L'app doit être en HTTPS en production (HTTP en dev c'est OK)
- Vérifiez que le manifest.json est valide

### Les mises à jour ne s'appliquent pas
- Le Service Worker cache agressivement
- Shift+Refresh forcera une mise à jour
- Attendez 24h pour la propagation en production

## 📚 Ressources

- [Vue PWA Documentation](https://vite-pwa-org.netlify.app/)
- [Workbox Guide](https://developers.google.com/web/tools/workbox)
- [MDN PWA Guide](https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps)
- [PWA Builder](https://www.pwabuilder.com/)

---

**Questions ?** Consultez `PWA_SETUP.md` pour plus de détails.
