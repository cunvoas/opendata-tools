# Service d'Authentification JWT pour Application Mobile

## Résumé

Un système d'authentification JWT complet a été implémenté pour permettre aux applications mobiles de se connecter de manière sécurisée à l'API. Ce système utilise Spring Boot et Spring Security avec JWT (JSON Web Tokens).

## Fichiers Créés

### 1. Configuration et Sécurité

- **`config/security/jwt/JwtUtil.java`** : Utilitaire pour générer et valider les tokens JWT
- **`config/security/jwt/JwtAuthenticationFilter.java`** : Filtre pour intercepter et valider les tokens JWT dans chaque requête
- **`config/security/JwtSecurityConfig.java`** : Configuration de sécurité pour les endpoints API (/api/**)

### 2. Modèles et Entités

- **`model/auth/RefreshToken.java`** : Entité JPA pour stocker les refresh tokens (modifiée pour utiliser Contributeur)

### 3. Repositories

- **`repo/auth/RefreshTokenRepository.java`** : Repository pour gérer les refresh tokens (adapté pour Contributeur)

### 4. Services

- **`service/auth/JwtAuthService.java`** : Service métier pour l'authentification, la génération et la validation des tokens

### 5. DTOs

- **`controller/dto/auth/LoginRequest.java`** : DTO pour la requête de connexion
- **`controller/dto/auth/AuthResponse.java`** : DTO pour la réponse d'authentification avec tokens
- **`controller/dto/auth/RefreshTokenRequest.java`** : DTO pour la requête de renouvellement de token

### 6. Contrôleurs REST

- **`controller/rest/AuthController.java`** : Contrôleur REST exposant les endpoints d'authentification

### 7. Exceptions

- **`exception/RefreshTokenException.java`** : Exception personnalisée pour les erreurs de refresh token

### 8. Documentation

- **`doc/API-JWT-Authentication.md`** : Documentation complète de l'API avec exemples d'utilisation
- **`resources/sql/create_refresh_token_table.sql`** : Script SQL pour créer la table des refresh tokens

## Fichiers Modifiés

### 1. **pom.xml**
Ajout des dépendances JWT :
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### 2. **application.yml**
Ajout de la configuration JWT :
```yaml
jwt:
  secret: ${JWT_SECRET:...}
  expiration: ${JWT_EXPIRATION:3600000}  # 1 heure
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:86400000}  # 24 heures
```

## Endpoints API Créés

### Authentification

| Méthode | Endpoint | Description | Auth requise |
|---------|----------|-------------|--------------|
| POST | `/api/auth/login` | Connexion utilisateur | Non |
| POST | `/api/auth/refresh` | Renouvellement du token | Non |
| POST | `/api/auth/logout` | Déconnexion utilisateur | Oui |
| GET | `/api/auth/me` | Infos utilisateur connecté | Oui |

## Architecture de Sécurité

### Dual Security Configuration

L'application dispose maintenant de **deux chaînes de filtres de sécurité** :

1. **API REST JWT** (`@Order(0)`) - Pour `/api/**`
   - Authentification par JWT
   - Session stateless
   - CSRF désactivé
   - Pour applications mobiles

2. **Application Web MVC** (`@Order(1)`) - Pour les autres endpoints
   - Authentification par formulaire
   - Session avec cookies
   - Protection CSRF activée
   - Pour l'interface web

### Flux d'Authentification

```
1. Login → POST /api/auth/login {username, password}
   ↓
2. Réponse → {accessToken, refreshToken, userInfo}
   ↓
3. Requêtes API → Header: Authorization: Bearer <accessToken>
   ↓
4. Token expiré → POST /api/auth/refresh {refreshToken}
   ↓
5. Nouveau token → {accessToken, refreshToken}
```

### Tokens

- **Access Token** (JWT)
  - Durée : 1 heure par défaut
  - Contient : username, authorities
  - Stocké : en mémoire côté client
  - Usage : authentification des requêtes API

- **Refresh Token**
  - Durée : 24 heures par défaut
  - Stocké : en base de données
  - Usage : renouveler l'access token

## Configuration Requise

### 1. Base de Données

Exécuter le script SQL :
```bash
psql -U postgis -d postgis -f src/main/resources/sql/create_refresh_token_table.sql
```

### 2. Variables d'Environnement

Définir dans votre environnement ou fichier `.env` :

```bash
# Générer une clé secrète
JWT_SECRET=$(openssl rand -base64 64)

# Durées en millisecondes
JWT_EXPIRATION=3600000        # 1 heure
JWT_REFRESH_EXPIRATION=86400000  # 24 heures
```

## Sécurité

### Fonctionnalités de Sécurité

✅ Tokens JWT signés avec HMAC-SHA256
✅ Refresh tokens stockés en base de données
✅ Révocation des tokens lors du logout
✅ Expiration automatique des tokens
✅ Validation stricte des tokens à chaque requête
✅ Session stateless pour les API REST
✅ Exception personnalisée pour une meilleure gestion d'erreurs

### Bonnes Pratiques Implémentées

- Utilisation de `@AuthenticationPrincipal` pour récupérer l'utilisateur
- Logs détaillés des événements d'authentification
- Gestion transactionnelle des tokens
- Nettoyage automatique des tokens expirés
- Séparation claire entre API mobile et application web

## Tests

### Avec cURL

```bash
# Login
curl -X POST http://localhost:8980/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# Refresh Token
curl -X POST http://localhost:8980/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<token>"}'

# API Protégée
curl -X GET http://localhost:8980/api/auth/me \
  -H "Authorization: Bearer <accessToken>"

# Logout
curl -X POST http://localhost:8980/api/auth/logout \
  -H "Authorization: Bearer <accessToken>"
```

## Intégration Mobile

Consultez la documentation complète dans `/doc/API-JWT-Authentication.md` pour des exemples d'intégration avec :
- React Native
- Flutter
- iOS natif (Swift)
- Android natif (Kotlin)

## Prochaines Étapes Recommandées

1. **Endpoint d'inscription** : Ajouter `/api/auth/register` pour créer de nouveaux comptes
2. **Réinitialisation de mot de passe** : Implémenter le flow de reset password via email
3. **Multi-device** : Permettre plusieurs refresh tokens par utilisateur
4. **Rate limiting** : Limiter les tentatives de connexion
5. **2FA** : Ajouter l'authentification à deux facteurs
6. **Swagger/OpenAPI** : Documenter l'API avec Swagger UI

## Support

Pour toute question ou problème :
- Consultez la documentation : `/doc/API-JWT-Authentication.md`
- Vérifiez les logs : recherchez "JWT" dans les logs de l'application
- Tests : Utilisez les exemples cURL fournis

## Analyse de Code

✅ Tous les fichiers ont été analysés avec Codacy
✅ Aucune vulnérabilité détectée (Trivy)
✅ Aucun problème de qualité de code (PMD)
✅ Code conforme aux bonnes pratiques Spring Security
