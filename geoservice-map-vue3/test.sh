export VITE_FEATURE_NDVI=true

# Source des données (build) : static = JSON publiés sur GitHub, dynamic = API locale (port 8980)
# VITE_DATA_MODE static ou dynamic
export VITE_DATA_MODE=static
#export VITE_DATA_MODE=dynamic

export VITE_STATIC_ON_GIT=https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main
# Proxy Vite (dev) : true = le front appelle /isochrone sur son origine, Vite redirige vers le backend (pas de CORS)
#                    false = appel direct au backend, CORS requis


export VITE_DEV_PROXY=false
#export VITE_DEV_PROXY=true
if [ "$VITE_DEV_PROXY" = "true" ]; then
  export VITE_DYNAMIC_API=/isochrone
else
  export VITE_DYNAMIC_API=http://localhost:8980/isochrone
fi

