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


