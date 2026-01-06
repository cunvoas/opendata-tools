# API d'Authentification JWT - Documentation

## Vue d'ensemble

Ce service d'authentification JWT permet aux applications mobiles de se connecter de manière sécurisée à l'API via des tokens JWT (JSON Web Tokens). Il utilise un système à deux tokens :
- **Access Token** : Token de courte durée (1 heure) pour authentifier les requêtes API
- **Refresh Token** : Token de longue durée (24 heures) pour renouveler les access tokens

## Configuration

### Variables d'environnement

Configurez les variables suivantes dans votre fichier `.env` ou `application.yml` :

```yaml
jwt:
  secret: ${JWT_SECRET}  # Clé secrète d'au moins 256 bits (base64)
  expiration: ${JWT_EXPIRATION:3600000}  # 1 heure en ms
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:86400000}  # 24 heures en ms
```

### Génération de la clé secrète

Pour générer une clé secrète sécurisée (base64, 256 bits minimum) :

```bash
openssl rand -base64 64
```

### Base de données

Exécutez le script SQL pour créer la table des refresh tokens :

```bash
psql -U postgis -d postgis -f src/main/resources/sql/create_refresh_token_table.sql
```

## Endpoints API

Tous les endpoints d'authentification sont préfixés par `/api/auth`.

### 1. Login (Connexion)

Authentifie un utilisateur et retourne les tokens JWT.

**Endpoint:** `POST /api/auth/login`

**Corps de la requête:**
```json
{
  "username": "john.doe",
  "password": "motdepasse123"
}
```

**Réponse réussie (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 123,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "role": "USER"
}
```

**Erreur (401 Unauthorized):**
```json
"Identifiants invalides"
```

### 2. Refresh Token (Renouvellement)

Renouvelle l'access token en utilisant un refresh token valide.

**Endpoint:** `POST /api/auth/refresh`

**Corps de la requête:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Réponse réussie (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "660f9511-f30c-52e5-b827-557766551111",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 123,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "role": "USER"
}
```

**Erreur (401 Unauthorized):**
```json
"Refresh token invalide ou expiré"
```

### 3. Logout (Déconnexion)

Révoque tous les tokens d'un utilisateur.

**Endpoint:** `POST /api/auth/logout`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Réponse réussie (200 OK):**
```json
"Déconnexion réussie"
```

### 4. Get Current User (Utilisateur actuel)

Récupère les informations de l'utilisateur actuellement authentifié.

**Endpoint:** `GET /api/auth/me`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Réponse réussie (200 OK):**
```json
{
  "userId": 123,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "role": "USER"
}
```

## Utilisation dans une application mobile

### 1. Flux de connexion initial

```typescript
// Exemple en TypeScript/JavaScript
async function login(username: string, password: string) {
  const response = await fetch('http://localhost:8980/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password })
  });
  
  if (response.ok) {
    const data = await response.json();
    // Stocker les tokens de manière sécurisée
    await secureStorage.setItem('accessToken', data.accessToken);
    await secureStorage.setItem('refreshToken', data.refreshToken);
    return data;
  } else {
    throw new Error('Authentification échouée');
  }
}
```

### 2. Appel API authentifié

```typescript
async function callProtectedAPI(endpoint: string) {
  const accessToken = await secureStorage.getItem('accessToken');
  
  const response = await fetch(`http://localhost:8980${endpoint}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    }
  });
  
  if (response.status === 401) {
    // Token expiré, essayer de le renouveler
    await refreshAccessToken();
    // Réessayer la requête
    return callProtectedAPI(endpoint);
  }
  
  return response.json();
}
```

### 3. Renouvellement automatique du token

```typescript
async function refreshAccessToken() {
  const refreshToken = await secureStorage.getItem('refreshToken');
  
  const response = await fetch('http://localhost:8980/api/auth/refresh', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refreshToken })
  });
  
  if (response.ok) {
    const data = await response.json();
    await secureStorage.setItem('accessToken', data.accessToken);
    await secureStorage.setItem('refreshToken', data.refreshToken);
    return data;
  } else {
    // Refresh token invalide, rediriger vers la page de login
    await logout();
    throw new Error('Session expirée');
  }
}
```

### 4. Déconnexion

```typescript
async function logout() {
  const accessToken = await secureStorage.getItem('accessToken');
  
  try {
    await fetch('http://localhost:8980/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
      }
    });
  } catch (error) {
    // Ignorer les erreurs de déconnexion
  } finally {
    // Supprimer les tokens localement
    await secureStorage.removeItem('accessToken');
    await secureStorage.removeItem('refreshToken');
  }
}
```

## Sécurité

### Bonnes pratiques

1. **Stockage des tokens**
   - iOS : Utilisez le Keychain
   - Android : Utilisez EncryptedSharedPreferences
   - Ne jamais stocker les tokens en clair

2. **HTTPS obligatoire**
   - Toujours utiliser HTTPS en production
   - Ne jamais transmettre de tokens sur HTTP

3. **Rotation des clés**
   - Changez régulièrement la clé secrète JWT
   - Implémentez une stratégie de rotation des refresh tokens

4. **Gestion de session**
   - Déconnectez automatiquement après expiration du refresh token
   - Permettez la révocation manuelle des tokens

5. **Protection CSRF**
   - CSRF désactivé pour les API REST (stateless)
   - Tokens JWT comme seule méthode d'authentification

## Tests avec cURL

### Login
```bash
curl -X POST http://localhost:8980/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

### Refresh Token
```bash
curl -X POST http://localhost:8980/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"550e8400-e29b-41d4-a716-446655440000"}'
```

### API Protégée
```bash
curl -X GET http://localhost:8980/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Logout
```bash
curl -X POST http://localhost:8980/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Codes d'erreur

| Code | Description |
|------|-------------|
| 200  | Succès |
| 400  | Requête invalide (validation échouée) |
| 401  | Non authentifié ou token invalide |
| 403  | Accès interdit (permissions insuffisantes) |
| 500  | Erreur serveur interne |

## Dépannage

### Problème : "Token expiré"
- Solution : Utilisez le refresh token pour obtenir un nouveau token

### Problème : "Refresh token invalide"
- Solution : L'utilisateur doit se reconnecter

### Problème : "Erreur de signature JWT"
- Solution : Vérifiez que la clé secrète JWT est correctement configurée

### Problème : "CORS error"
- Solution : Configurez les origins autorisées dans `application.yml`

## Support et contact

Pour toute question ou problème, consultez la documentation complète ou contactez l'équipe de développement.
