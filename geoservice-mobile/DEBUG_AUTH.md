# Guide de débogage - Authentification

## Logs ajoutés

Des logs détaillés ont été ajoutés pour faciliter le débogage des problèmes d'authentification. Les logs utilisent des icônes pour une meilleure lisibilité:

- 🔐 **AuthService** - Logs du service d'authentification
- 🔒 **AuthProvider** - Logs du provider d'état
- 🔑 **LoginScreen** - Logs de l'écran de connexion
- ✅ Succès
- ❌ Erreur
- 📤 Envoi de requête
- 📥 Réception de réponse
- 🔄 Renouvellement de token

## Comment voir les logs

### Pendant le développement

Lancez l'application avec:
```bash
flutter run
```

Les logs apparaîtront dans votre terminal pendant l'exécution.

### Filtrer les logs

Pour voir uniquement les logs d'authentification:
```bash
flutter run | grep -E "\[Auth|🔐|🔒|🔑"
```

## Checklist de débogage

Lorsque l'authentification échoue, vérifiez dans l'ordre:

### 1. Logs d'initialisation
```
🔒 [AuthProvider] Initialisation du AuthProvider
🔒 [AuthProvider] BaseURL: http://localhost:8080
```

**Vérifiez:**
- ✅ L'URL du serveur est-elle correcte?
- ✅ Le serveur est-il accessible depuis votre appareil?

### 2. Logs de tentative de connexion
```
🔑 [LoginScreen] Début de la connexion
   Server: http://localhost:8080
   Username: john.doe
```

**Vérifiez:**
- ✅ Le nom d'utilisateur est correct
- ✅ Le serveur est bien celui attendu

### 3. Logs du service d'authentification
```
🔐 [AuthService] Tentative de connexion...
   URL: http://localhost:8080/isochrone/api/auth/login
   Username: john.doe
   URL complète: http://localhost:8080/isochrone/api/auth/login
```

**Vérifiez:**
- ✅ L'URL complète est correctement formée
- ✅ Le endpoint `/isochrone/api/auth/login` existe sur le serveur

### 4. Logs de requête HTTP
```
📤 [AuthService] Envoi de la requête POST...
   Body: {"username":"john.doe","password":"********"}
```

**Vérifiez:**
- ✅ Le body est bien formaté en JSON
- ✅ Les données sont correctes

### 5. Logs de réponse HTTP
```
📥 [AuthService] Réponse reçue
   Status Code: 200
   Headers: {content-type: application/json, ...}
   Body: {"accessToken":"...", "refreshToken":"...", ...}
```

**Vérifiez:**
- ✅ Status code = 200 pour succès
- ✅ Status code = 401 pour identifiants invalides
- ✅ Le body contient les tokens JWT

## Erreurs courantes

### Erreur de connexion
```
❌ [AuthService] Erreur de connexion HTTP
   Exception: Connection refused
```

**Solution:**
- Vérifiez que le serveur backend est lancé
- Vérifiez l'URL (http://localhost:8080 ne marche que sur l'émulateur)
- Pour appareil physique, utilisez l'IP locale (ex: http://192.168.1.10:8080)

### Erreur 401 - Identifiants invalides
```
❌ [AuthService] Identifiants invalides (401)
```

**Solution:**
- Vérifiez les identifiants dans la base de données backend
- Vérifiez que l'utilisateur existe et est actif
- Vérifiez que le mot de passe est correct

### Erreur 404 - Endpoint introuvable
```
❌ [AuthService] Erreur HTTP: 404
```

**Solution:**
- Vérifiez que le backend utilise bien `/isochrone/api/auth/login`
- Vérifiez la configuration du backend
- Testez l'URL avec curl ou Postman

### Erreur CORS (depuis le navigateur web)
```
❌ [AuthService] Erreur inattendue
   Exception: XMLHttpRequest error
```

**Solution:**
- Configurez CORS sur le backend pour accepter les requêtes depuis localhost
- Ajoutez les headers CORS appropriés dans le backend

### Erreur de parsing JSON
```
❌ [AuthService] Erreur inattendue
   Exception: FormatException: Unexpected character
```

**Solution:**
- Vérifiez que le backend retourne bien du JSON
- Vérifiez que la structure JSON correspond au modèle AuthResponse

## Test avec curl

Pour tester l'API indépendamment de l'app Flutter:

```bash
curl -X POST http://localhost:8080/isochrone/api/auth/login \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"username":"john.doe","password":"password123"}' \
  -v
```

**Réponse attendue:**
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

## Activer/Désactiver les logs

Les logs utilisent `debugPrint()` qui est automatiquement désactivé en mode release.

Pour désactiver les logs manuellement en développement, vous pouvez:

1. Commenter les lignes `debugPrint()` dans les fichiers
2. Ou créer une constante globale:

```dart
// lib/config/app_config.dart
const bool enableAuthLogs = false; // Mettre à false pour désactiver

// Puis dans les services:
if (enableAuthLogs) {
  debugPrint('...');
}
```

## Logging avancé

Pour des logs plus sophistiqués, considérez l'utilisation de packages:

- **logger** - Logs colorés et structurés
- **f_logs** - Logs avec sauvegarde sur fichier
- **sentry_flutter** - Reporting d'erreurs en production

Exemple avec logger:
```bash
flutter pub add logger
```

```dart
import 'package:logger/logger.dart';

final logger = Logger();

logger.d('Debug message');
logger.i('Info message');
logger.w('Warning message');
logger.e('Error message');
```

## Connexion avec IP locale

Si vous testez sur un appareil physique:

1. Trouvez votre IP locale:
   ```bash
   # Sur Linux/Mac
   ifconfig | grep inet
   
   # Sur Windows
   ipconfig
   ```

2. Utilisez cette IP dans l'app:
   ```
   http://192.168.1.10:8080
   ```

3. Assurez-vous que le firewall autorise les connexions

## Problèmes spécifiques

### Android - Cleartext traffic not permitted

Si vous voyez cette erreur, ajoutez dans `android/app/src/main/AndroidManifest.xml`:

```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

### iOS - App Transport Security

Si vous utilisez HTTP (non HTTPS) sur iOS, ajoutez dans `ios/Runner/Info.plist`:

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

**⚠️ Note:** Ces configurations ne doivent être utilisées qu'en développement. En production, utilisez HTTPS.

## Support

Si les problèmes persistent après avoir vérifié tous ces points:

1. Capturez les logs complets
2. Testez l'API avec curl
3. Vérifiez les logs du backend
4. Comparez avec la documentation API du backend
