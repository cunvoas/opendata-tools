# geoservice-map-vue3

 Ce projet est l'application publique déployée pour Aut'MEL.

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run dev
```

### Compiles and minifies for production
```
npm run build
```

### Lints and fixes files
```
npm run lint
```

### Feature flags

Une fonctionnalité en cours de déploiement est activée au build par une variable Vite `VITE_FEATURE_<NOM>=true|false` (déclarée dans `src/utils/config.js`).
Sans valeur, elle est **active en développement** (`npm run dev`) et **inactive dans un build de production** :
rien n'est exposé tant qu'on ne l'active pas explicitement.

| Variable | Fonctionnalité | Quand elle est inactive |
|----------|----------------|-------------------------|
| `VITE_FEATURE_NDVI` | écran NDVI (`/ndvi`) | la route n'existe pas (l'URL retombe sur `/carte`) et les liens « NDVI » de l'en-tête et du pied de page sont masqués |

Activer l'écran NDVI dans un build :
```
VITE_FEATURE_NDVI=true npm run build
```
(ou passer `export VITE_FEATURE_NDVI=true` dans `deployGhP.sh`, qui le fixe à `false` par défaut).
En local, `.env.local` (ignoré par git) permet de forcer une valeur, par exemple `VITE_FEATURE_NDVI=false`.

Le flag couvre aussi la carte : inactif, le bouton NDVI, les informations NDVI des parcs (infobulle, détail) et la légende NDVI sont masqués.

### Source des données

Les écrans carte, isochrone, NDVI et stats lisent leurs données depuis `src/utils/config.js` (source des données et feature flags, une seule configuration) :

| Variable | Valeur par défaut | Rôle |
|----------|-------------------|------|
| `VITE_DATA_MODE` | `static` | `static` : JSON publiés sur GitHub ; `dynamic` : API locale (`http://localhost:8980/isochrone`) |
| `VITE_STATIC_ON_GIT` | dépôt `autmel/geoservice-data` | racine des JSON publiés |
| `VITE_DYNAMIC_API` | `http://localhost:8980/isochrone` | racine de l'API locale (mode `dynamic`), sans barre finale |
| `VITE_DEV_PROXY` | `false` | dev uniquement, positionné dans `test.sh` : `true` fait passer `/isochrone` par le proxy Vite (pas de CORS) et règle `VITE_DYNAMIC_API=/isochrone` ; `false` appelle le backend directement |

`deployGhP.sh` les fixe explicitement pour le déploiement. Pour travailler avec le backend local, mettre `VITE_DATA_MODE=dynamic` dans `.env.local`. Les stats n'ont pas d'API : elles lisent toujours les JSON publiés.


### Grille NDVI

Les seuils, libellés et couleurs NDVI (seuils universels par type de surface) sont définis une seule fois dans `src/utils/ndvi.js` :
écran NDVI, couleurs et légende de la carte, détail d'un parc.
La popup de la carte de consultation du backend (`geoservice-isochrone-v2`, `consultMap.html`) en garde une copie,
vérifiée par `src/utils/__tests__/ndvi.test.js` : une modification de la grille doit être reportée aux deux endroits.

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).
See [OpenStreetMap Zoom](https://wiki.openstreetmap.org/wiki/Zoom_levels).

## Depannage: erreur `Cannot find package 'vite'`

Si le serveur de dev echoue apres une mise a jour de securite avec une erreur de type:
`failed to load config ... Cannot find package 'vite'`, executez:

```bash
npm ci
npm ls vite --depth=0
npm run dev
```

Notes:
- `npm ci` reinstalle proprement toutes les dependances depuis `package-lock.json`.
- `npm ls vite --depth=0` permet de verifier que `vite` est bien installe.


