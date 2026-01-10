# Formulaire de Login - GeoService Mobile

## Vue d'ensemble

Ce document décrit l'implémentation du formulaire de login pour l'application mobile GeoService Flutter. Le formulaire communique avec l'API d'authentification JWT fournie par le service backend Java `geoservice-isochrone`.

## Architecture

### Modèles (Models)
- **[AuthResponse](lib/models/auth_response.dart)** - Représente la réponse d'authentification avec les tokens JWT
- **[LoginRequest](lib/models/login_request.dart)** - Représente la requête de login

### Services (Services)
- **[AuthService](lib/services/auth_service.dart)** - Gère la communication HTTP avec l'API d'authentification
  - `login()` - Effectue la connexion
  - `refreshToken()` - Renouvelle le token d'accès
  - `validateToken()` - Valide un token

### Providers (State Management)
- **[AuthProvider](lib/providers/auth_provider.dart)** - Gère l'état d'authentification de l'application
  - Utilise le package `provider` pour la gestion d'état réactive
  - Notifie les widgets des changements d'authentification

### Écrans (Screens)
- **[LoginScreen](lib/screens/login_screen.dart)** - Interface utilisateur du formulaire de login
  - Champs de saisie pour le serveur, nom d'utilisateur et mot de passe
  - Validation du formulaire
  - Indicateur de chargement
  - Gestion des erreurs

## API d'authentification

### Endpoint
```
POST http(s)://{server_fqdn}/isochrone/api/auth/login
```

### Requête
```json
{
  "username": "john.doe",
  "password": "password123"
}
```

### Réponse réussie (200 OK)
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

## Configuration

### Variables d'environnement

Vous pouvez configurer l'URL du serveur de plusieurs façons:

1. **Dans le fichier de configuration** (`lib/config/app_config.dart`)
   ```dart
   static const String defaultServerUrl = 'http://localhost:8080';
   ```

2. **Lors de la création du provider**
   ```dart
   AuthProvider(baseUrl: 'https://server.example.com')
   ```

3. **Depuis l'écran de login**
   Le champ "Serveur" permet à l'utilisateur de saisir l'adresse du serveur

## Dépendances

Le projet utilise les packages suivants:

- **[provider](https://pub.dev/packages/provider)** ^6.1.0 - Gestion d'état réactive
- **[http](https://pub.dev/packages/http)** ^1.1.0 - Client HTTP pour les requêtes API

Pour installer les dépendances:
```bash
flutter pub get
```

## Structure des fichiers

```
lib/
├── config/
│   └── app_config.dart              # Configuration de l'application
├── models/
│   ├── auth_response.dart           # Modèle de réponse d'authentification
│   └── login_request.dart           # Modèle de requête de login
├── providers/
│   └── auth_provider.dart           # Provider pour l'état d'authentification
├── screens/
│   └── login_screen.dart            # Écran de connexion
├── services/
│   └── auth_service.dart            # Service d'authentification API
└── main.dart                        # Point d'entrée de l'application
```

## Utilisation

### Accéder à l'état d'authentification

```dart
final authProvider = Provider.of<AuthProvider>(context);
if (authProvider.isAuthenticated) {
  print('Utilisateur connecté: ${authProvider.currentUser?.username}');
}
```

### Effectuer une connexion

```dart
final authProvider = Provider.of<AuthProvider>(context, listen: false);
final success = await authProvider.login(
  username: 'john.doe',
  password: 'password123',
);

if (success) {
  // Redirection vers l'écran principal
  Navigator.pushReplacementNamed(context, '/home');
}
```

### Renouveler le token

```dart
final authProvider = Provider.of<AuthProvider>(context, listen: false);
final refreshed = await authProvider.refreshAccessToken();
if (!refreshed) {
  // Redirection vers le login
  Navigator.pushReplacementNamed(context, '/login');
}
```

## Gestion des erreurs

Le `AuthProvider` expose la propriété `error` qui contient le message d'erreur:

```dart
if (authProvider.error != null) {
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(content: Text(authProvider.error!)),
  );
}
```

## Sécurité

### Points importants
1. **HTTPS** - Utilisez HTTPS en production pour sécuriser les échanges
2. **Tokens JWT** - Les tokens sont stockés en mémoire (pour production, considérez le stockage sécurisé avec `flutter_secure_storage`)
3. **Expiration** - Implémentez la logique de renouvellement des tokens avant expiration

### Recommandations
- Stocker les tokens dans un stockage sécurisé
- Implémenter un refresh token automatique
- Valider les tokens avant chaque requête API
- Implémenter un timeout d'inactivité

## Développement

### Exécution
```bash
flutter run
```

### Tests
```bash
flutter test
```

### Analyse de code
```bash
flutter analyze
```

## Intégration avec d'autres services

Pour intégrer d'autres services API:

1. Créez un nouveau modèle dans `lib/models/`
2. Créez un nouveau service dans `lib/services/`
3. Créez un nouveau provider dans `lib/providers/`
4. Utilisez les tokens depuis `authProvider.currentUser?.accessToken` pour l'authentification

Exemple:
```dart
final response = await http.get(
  Uri.parse('$baseUrl/api/endpoint'),
  headers: {
    'Authorization': 'Bearer ${authProvider.currentUser?.accessToken}',
    'Content-Type': 'application/json',
  },
);
```

## Dépannage

### La connexion échoue
- Vérifiez l'URL du serveur
- Vérifiez que le service backend est en cours d'exécution
- Vérifiez les identifiants
- Consultez les logs pour plus de détails

### Les tokens ne sont pas valides
- Vérifiez la configuration JWT du backend
- Vérifiez que les tokens ne sont pas expirés
- Utilisez `refreshAccessToken()` pour renouveler

### Erreurs CORS
- Configurez CORS correctement sur le backend
- Assurez-vous que le backend accepte les requêtes du client
