# Client Copernicus NDVI — OAuth et token

Intégration CDSE pour `com.github.cunvoas.geoserviceisochrone.extern.ndvi.ClientCopernicusNdvi`
(NDVI moyen d'un polygone de parc via l'API openEO synchrone).

## Références CDSE

| Sujet | Lien |
|---|---|
| Création / administration du compte | https://documentation.dataspace.copernicus.eu/Registration.html |
| Génération du token (cURL) | https://documentation.dataspace.copernicus.eu/APIs/Token.html |
| openEO Client Credentials | https://documentation.dataspace.copernicus.eu/APIs/openEO/authentication/client_credentials.html |
| Rate limiting (contrat d'appel) | https://documentation.dataspace.copernicus.eu/APIs/SentinelHub/Overview/RateLimiting.html |

## Endpoints

| Usage | URL |
|---|---|
| Token OIDC (Keycloak) | `https://identity.dataspace.copernicus.eu/auth/realms/CDSE/protocol/openid-connect/token` |
| API openEO (calcul NDVI) | `https://openeo.dataspace.copernicus.eu/openeo/1.1` (`POST /result` = calcul synchrone) |
| Capabilities openEO | `GET https://openeo.dataspace.copernicus.eu/openeo/1.1/` |

## Flux implémenté

`ClientCopernicusNdvi` (Spring `RestClient` + `@Value("${copernicus.openeo.client-id}")` / `client-secret`) :

1. `POST <idpDataSpace>/token` en `application/x-www-form-urlencoded` :
   `grant_type=client_credentials`, `client_id`, `client_secret`
2. Réponse `CdsTokenResponse` : `access_token`, `expires_in`, `refresh_token`
   (Jackson 3 `tools.jackson.databind.ObjectMapper`, annotations `com.fasterxml.jackson.annotation`)
3. Le token est mis en cache (`getAccessToken()`, `synchronized`) et **recyclé pour tous
   les appels jusqu'à expiration** ; renouvellement automatique 30 s avant l'échéance
   (`TOKEN_EXPIRY_MARGIN`)
4. Chaque appel openEO porte `Authorization: Bearer <token>`
5. Rate limiting via `RateLimitingInterceptor` (couture de test : constructeur
   `RateLimitingInterceptor(RateLimiter)` ; no-arg par défaut 4 req/s)

Tests : `TestOpenEOResponse` (désérialisation records), `TestRateLimitInterceptor` (retry 429).

## Configuration

```yaml
copernicus:
  openeo:
    client-id: ${CDSE_CLIENT_ID}      # via variables d'env, pas en clair dans le repo
    client-secret: ${CDSE_CLIENT_SECRET}
```

`client_id` / `client_secret` = compte **OAuth client** créé sur le
[Dashboard CDSE](https://dataspace.copernicus.eu/dashboard) → *OAuth Clients → Create*.

## Générer un token à la main (cURL)

### Flow `client_credentials` (comme l'application)

```bash
curl -s -X POST 'https://identity.dataspace.copernicus.eu/auth/realms/CDSE/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials' \
  -d 'client_id=VOTRE_OAUTH_CLIENT_ID' \
  -d 'client_secret=VOTRE_OAUTH_CLIENT_SECRET'
```

### Flow mot de passe (compte portail simple)

`client_id` reste `cdse-public` ; `username` = **email du portail**.

```bash
curl -s -X POST 'https://identity.dataspace.copernicus.eu/auth/realms/CDSE/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=cdse-public' \
  -d 'username=VOTRE_EMAIL_CDSE' \
  -d 'password=VOTRE_MOT_DE_PASSE' \
  -d 'grant_type=password'
```

2FA activé → ajouter `-d 'totp=123456'` (code de l'app authentificatrice, fenêtre ~30 s),
puis recycler via le refresh pour éviter de retaper le TOTP :

```bash
curl -s -X POST 'https://identity.dataspace.copernicus.eu/auth/realms/CDSE/protocol/openid-connect/token' \
  -d 'grant_type=refresh_token' \
  -d 'client_id=cdse-public' \
  -d 'refresh_token=LE_REFRESH_TOKEN_OBTENU'
```

Réponse attendue : JSON avec `access_token`, `expires_in`, `token_type: Bearer`
(token court, ~10 min → recyclé par l'application).

## Erreurs fréquentes

| Erreur | Cause | Correctif |
|---|---|---|
| `{"error":"invalid_grant","error_description":"Invalid user credentials"}` | username/password faux (ou `totp` faux) | email du portail + password entre guillemets simples `'...'` (protège `$`, `!`, backtick) |
| même erreur avec `grant_type=client_credentials` | le compte n'est pas un OAuth client | créer le client sur le Dashboard (voir ci-dessus), ou passer au flow mot de passe |
| `unauthorized_client` | client_id inconnu du realm CDSE | vérifier le client_id exact |
| 429 sur les appels openEO | quota dépassé | géré par `RateLimitingInterceptor` (retry après `retry-after`) |
