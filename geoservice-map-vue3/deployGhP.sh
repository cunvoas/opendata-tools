#!/bin/sh
# deploy on github

export DIST_PATH=/work/PERSO/github/opendata-tools/geoservice-map-vue3
export GH_PAGES_LOCAL_PATH=/work/PERSO/github/gh_pages/parcs-et-jardins

# Feature flags (build) : décommenter pour activer l'écran NDVI dans ce déploiement
export VITE_FEATURE_NDVI=false

# Source des données (build) : static = JSON publiés sur GitHub, dynamic = API locale (port 8980)
# VITE_DATA_MODE static ou dynamic
export VITE_DATA_MODE=static
export VITE_STATIC_ON_GIT=https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main
export VITE_DYNAMIC_API=http://localhost:8980/isochrone/

# Pré-rendu SEO (vite-plugin-prerender) : puppeteer 1.x embarque un Chromium trop ancien pour ES2021
export PUPPETEER_EXECUTABLE_PATH=/home/cus/.cache/puppeteer/chrome/linux-156.0.8078.47/chrome-linux64/chrome

npm run build
rm -Rf $GH_PAGES_LOCAL_PATH/*.html
rm -Rf $GH_PAGES_LOCAL_PATH/assets/*

cp -Rf $DIST_PATH/dist/* $GH_PAGES_LOCAL_PATH/
cp -f  $DIST_PATH/404.html $GH_PAGES_LOCAL_PATH/
cp -f  $DIST_PATH/favicon.ico $GH_PAGES_LOCAL_PATH/

# NDVI conditionnel : sans le flag, on retire l'ancienne page publiée
if [ "$VITE_FEATURE_NDVI" = "true" ]; then
  echo "NDVI activé : /ndvi déployé"
else
  rm -Rf $GH_PAGES_LOCAL_PATH/ndvi
  echo "NDVI désactivé : /ndvi retiré de $GH_PAGES_LOCAL_PATH"
fi

echo "cd ${GH_PAGES_LOCAL_PATH}"
echo 'git status'
echo 'git add .'
echo 'git commit -m "chore: TEXT"'
echo 'git push'









