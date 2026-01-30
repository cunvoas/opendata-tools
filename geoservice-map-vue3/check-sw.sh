#!/bin/bash

# Script de vérification rapide du Service Worker

echo "🔍 Vérification du Service Worker PWA..."
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Vérifier que dist/ existe
if [ ! -d "dist" ]; then
  echo -e "${RED}✗ dist/ n'existe pas${NC}"
  echo "  → Lancez: npm run build"
  exit 1
fi

echo -e "${GREEN}✓${NC} Dossier dist/ existe"
echo ""

# Vérifier les fichiers PWA
echo "📁 Fichiers PWA générés :"
echo ""

files=("sw.js" "registerSW.js" "workbox-354287e6.js" "manifest.webmanifest")
for file in "${files[@]}"; do
  if [ -f "dist/$file" ]; then
    size=$(du -h "dist/$file" | cut -f1)
    echo -e "  ${GREEN}✓${NC} dist/$file ($size)"
  else
    echo -e "  ${RED}✗${NC} dist/$file (MANQUANT)"
  fi
done

echo ""

# Vérifier index.html
if grep -q "registerSW.js" dist/index.html; then
  echo -e "${GREEN}✓${NC} registerSW.js est enregistré dans index.html"
else
  echo -e "${RED}✗${NC} registerSW.js n'est PAS dans index.html"
fi

if grep -q "manifest.webmanifest" dist/index.html; then
  echo -e "${GREEN}✓${NC} manifest.webmanifest est lié dans index.html"
else
  echo -e "${RED}✗${NC} manifest.webmanifest n'est PAS lié dans index.html"
fi

echo ""

# Vérifier les icônes
echo "🎨 Icônes PWA :"
icons=("icon-192x192.png" "icon-512x512.png" "icon-maskable-192x192.png" "icon-maskable-512x512.png")
found=0
for icon in "${icons[@]}"; do
  if [ -f "dist/icons/$icon" ]; then
    echo -e "  ${GREEN}✓${NC} dist/icons/$icon"
    ((found++))
  else
    echo -e "  ${YELLOW}⚠${NC} dist/icons/$icon (MANQUANT)"
  fi
done

if [ $found -lt 4 ]; then
  echo ""
  echo -e "${YELLOW}⚠ Certaines icônes manquent${NC}"
  echo "  → Lancez: npm install --save-dev sharp"
  echo "  → Puis: node generate-icons.js votre-logo.png"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Résumé
if [ $found -eq 4 ]; then
  echo -e "${GREEN}✓ Configuration PWA complète !${NC}"
  echo ""
  echo "Pour tester le Service Worker :"
  echo "  1. npm run preview"
  echo "  2. Ouvrir http://localhost:4173"
  echo "  3. Appuyer sur F12 → Application → Service Workers"
  echo ""
  echo "Note: Vous devez être en HTTPS (prod) ou http://localhost (dev)"
else
  echo -e "${YELLOW}⚠ Configuration PWA incomplète${NC}"
  echo ""
  echo "Étapes manquantes :"
  echo "  • Générer les icônes : node generate-icons.js logo.png"
  echo "  • Puis relancer : npm run build"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
