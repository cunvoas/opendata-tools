#!/bin/bash

# Script de test de l'API d'authentification
# Usage: ./test_auth.sh [server_url] [username] [password]

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Valeurs par défaut
SERVER_URL="http://localhost:8980"
USERNAME="${1:-john.doe}"
PASSWORD="${2:-password123}"

echo -e "${BLUE}🔐 Test d'authentification GeoService${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "Server:   ${YELLOW}$SERVER_URL${NC}"
echo -e "Username: ${YELLOW}$USERNAME${NC}"
echo -e "Password: ${YELLOW}********${NC}"
echo ""

# Endpoint complet
ENDPOINT="$SERVER_URL/isochrone/api/auth/login"
echo -e "${BLUE}📍 Endpoint: ${NC}$ENDPOINT"
echo ""

# Données de connexion
LOGIN_DATA="{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}"

echo -e "${YELLOW}📤 Envoi de la requête...${NC}"
echo ""

# Effectuer la requête avec curl
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$ENDPOINT" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d "$LOGIN_DATA" \
  2>&1)

# Extraire le code de statut (dernière ligne)
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | head -n -1)

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📥 Réponse${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Vérifier le code HTTP
if [ "$HTTP_CODE" == "200" ]; then
    echo -e "Status: ${GREEN}$HTTP_CODE ✅${NC}"
    echo ""
    echo -e "${GREEN}✅ Authentification réussie!${NC}"
    echo ""
    echo "Body:"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
elif [ "$HTTP_CODE" == "401" ]; then
    echo -e "Status: ${RED}$HTTP_CODE ❌${NC}"
    echo ""
    echo -e "${RED}❌ Identifiants invalides${NC}"
    echo ""
    echo "Body:"
    echo "$BODY"
elif [ "$HTTP_CODE" == "404" ]; then
    echo -e "Status: ${RED}$HTTP_CODE ❌${NC}"
    echo ""
    echo -e "${RED}❌ Endpoint non trouvé${NC}"
    echo ""
    echo "Vérifiez que:"
    echo "  - Le serveur backend est lancé"
    echo "  - L'URL est correcte: $ENDPOINT"
    echo ""
    echo "Body:"
    echo "$BODY"
elif [ "$HTTP_CODE" == "000" ] || [ -z "$HTTP_CODE" ]; then
    echo -e "Status: ${RED}Pas de réponse ❌${NC}"
    echo ""
    echo -e "${RED}❌ Impossible de contacter le serveur${NC}"
    echo ""
    echo "Vérifiez que:"
    echo "  - Le serveur backend est lancé"
    echo "  - Le serveur est accessible: $SERVER_URL"
    echo "  - Le firewall autorise les connexions"
    echo ""
    echo "Erreur:"
    echo "$BODY"
else
    echo -e "Status: ${RED}$HTTP_CODE ❌${NC}"
    echo ""
    echo -e "${RED}❌ Erreur inattendue${NC}"
    echo ""
    echo "Body:"
    echo "$BODY"
fi

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Test de connectivité
echo ""
echo -e "${BLUE}🔍 Tests de connectivité${NC}"
echo ""

# Ping du serveur
SERVER_HOST=$(echo $SERVER_URL | sed -E 's#https?://([^:/]+).*#\1#')
echo -n "Ping $SERVER_HOST... "
if ping -c 1 -W 2 "$SERVER_HOST" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ OK${NC}"
else
    echo -e "${RED}❌ ÉCHEC${NC}"
fi

# Test de la racine du serveur
echo -n "Test HTTP $SERVER_URL... "
ROOT_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$SERVER_URL" --max-time 5 2>&1)
if [ "$ROOT_RESPONSE" != "000" ] && [ ! -z "$ROOT_RESPONSE" ]; then
    echo -e "${GREEN}✅ Accessible (Code: $ROOT_RESPONSE)${NC}"
else
    echo -e "${RED}❌ Inaccessible${NC}"
fi

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
