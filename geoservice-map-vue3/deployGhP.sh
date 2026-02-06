#!/bin/sh
# deploy on github

export DIST_PATH=/work/PERSO/github/opendata-tools/geoservice-map-vue3
export GH_PAGES_LOCAL_PATH=/work/PERSO/github/gh_pages/parcs-et-jardins


npm run build
rm -Rf $GH_PAGES_LOCAL_PATH/*.html
rm -Rf $GH_PAGES_LOCAL_PATH/assets/*

cp -Rf $DIST_PATH/dist/* $GH_PAGES_LOCAL_PATH/
cp -f  $DIST_PATH/404.html $GH_PAGES_LOCAL_PATH/
cp -f  $DIST_PATH/favicon.ico $GH_PAGES_LOCAL_PATH/

echo "cd ${GH_PAGES_LOCAL_PATH}"
echo 'git status'
echo 'git add .'
echo 'git commit -m "chore: TEXT"'
echo 'git push'









