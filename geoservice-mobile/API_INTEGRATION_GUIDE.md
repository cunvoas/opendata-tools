# Guide d'intégration - Client HTTP authentifié

## Vue d'ensemble

Ce guide montre comment utiliser le client HTTP authentifié pour faire des requêtes API sécurisées avec le token JWT automatiquement ajouté.

## Utilisation basique

### 1. Créer un service API

```dart
import 'package:http/http.dart' as http;
import 'package:geoservice_mobile/services/http_client.dart';
import 'package:geoservice_mobile/providers/auth_provider.dart';

class GeoServiceApiClient {
  final AuthenticatedHttpClient client;
  final String baseUrl;

  GeoServiceApiClient({
    required AuthProvider authProvider,
    required this.baseUrl,
  }) : client = AuthenticatedHttpClient(authProvider: authProvider);

  /// Récupère les données de géolocalisation
  Future<List<Map<String, dynamic>>> getLocations() async {
    final response = await client.getAuthenticated(
      Uri.parse('$baseUrl/isochrone/api/locations'),
    );

    if (response.statusCode == 200) {
      // Traiter la réponse
      return jsonDecode(response.body);
    } else if (response.statusCode == 401) {
      throw Exception('Token expiré - reconnexion requise');
    } else {
      throw Exception('Erreur: ${response.statusCode}');
    }
  }

  /// Crée une nouvelle isochrone
  Future<Map<String, dynamic>> createIsochrone(
    Map<String, dynamic> data,
  ) async {
    final response = await client.postAuthenticated(
      Uri.parse('$baseUrl/isochrone/api/isochrones'),
      body: jsonEncode(data),
    );

    if (response.statusCode == 201) {
      return jsonDecode(response.body);
    } else {
      throw Exception('Erreur: ${response.statusCode}');
    }
  }

  /// Met à jour une isochrone existante
  Future<void> updateIsochrone(
    int id,
    Map<String, dynamic> data,
  ) async {
    final response = await client.putAuthenticated(
      Uri.parse('$baseUrl/isochrone/api/isochrones/$id'),
      body: jsonEncode(data),
    );

    if (response.statusCode != 200) {
      throw Exception('Erreur: ${response.statusCode}');
    }
  }

  /// Supprime une isochrone
  Future<void> deleteIsochrone(int id) async {
    final response = await client.deleteAuthenticated(
      Uri.parse('$baseUrl/isochrone/api/isochrones/$id'),
    );

    if (response.statusCode != 204) {
      throw Exception('Erreur: ${response.statusCode}');
    }
  }
}
```

### 2. Enregistrer le service dans le provider

Dans `lib/main.dart`:

```dart
MultiProvider(
  providers: [
    ChangeNotifierProvider(
      create: (_) => AuthProvider(
        baseUrl: 'http://localhost:8080',
      ),
    ),
    ProxyProvider<AuthProvider, GeoServiceApiClient>(
      update: (_, authProvider, __) => GeoServiceApiClient(
        authProvider: authProvider,
        baseUrl: 'http://localhost:8080',
      ),
    ),
  ],
  child: MaterialApp(
    // ...
  ),
);
```

### 3. Utiliser le service dans un widget

```dart
class LocationsScreen extends StatefulWidget {
  @override
  State<LocationsScreen> createState() => _LocationsScreenState();
}

class _LocationsScreenState extends State<LocationsScreen> {
  @override
  void initState() {
    super.initState();
    _loadLocations();
  }

  Future<void> _loadLocations() async {
    final apiClient = context.read<GeoServiceApiClient>();
    try {
      final locations = await apiClient.getLocations();
      // Utiliser les emplacements
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erreur: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Emplacements')),
      body: Center(
        child: ElevatedButton(
          onPressed: _loadLocations,
          child: const Text('Charger les emplacements'),
        ),
      ),
    );
  }
}
```

## Gestion des erreurs

### Erreur 401 - Token expiré

```dart
try {
  final data = await apiClient.getLocations();
} catch (e) {
  if (e.toString().contains('Token expiré')) {
    final authProvider = context.read<AuthProvider>();
    final refreshed = await authProvider.refreshAccessToken();
    
    if (refreshed) {
      // Réessayer la requête
      final data = await apiClient.getLocations();
    } else {
      // Rediriger vers login
      Navigator.pushReplacementNamed(context, '/login');
    }
  }
}
```

### Erreur réseau

```dart
try {
  final data = await apiClient.getLocations();
} on SocketException {
  showErrorDialog('Erreur réseau - vérifiez votre connexion');
} on TimeoutException {
  showErrorDialog('Timeout - le serveur met trop de temps à répondre');
} catch (e) {
  showErrorDialog('Erreur: $e');
}
```

## Meilleure pratique

### Créer un provider pour chaque service

```dart
class GeoServiceProvider extends ChangeNotifier {
  final GeoServiceApiClient apiClient;
  
  List<Location>? _locations;
  bool _isLoading = false;
  String? _error;

  GeoServiceProvider({required this.apiClient});

  List<Location>? get locations => _locations;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> loadLocations() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final data = await apiClient.getLocations();
      _locations = data.map((json) => Location.fromJson(json)).toList();
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
```

### Utiliser avec Consumer

```dart
Consumer<GeoServiceProvider>(
  builder: (context, provider, child) {
    if (provider.isLoading) {
      return const CircularProgressIndicator();
    }

    if (provider.error != null) {
      return Text('Erreur: ${provider.error}');
    }

    return ListView(
      children: provider.locations
              ?.map((location) => ListTile(title: Text(location.name)))
              .toList() ??
          [],
    );
  },
)
```

## Intercepteurs personnalisés

Vous pouvez créer des intercepteurs pour:
- Logger les requêtes
- Ajouter des en-têtes personnalisés
- Gérer les erreurs globales
- Implémenter un retry automatique

```dart
class LoggingHttpClient extends http.BaseClient {
  final http.Client _inner;

  LoggingHttpClient(this._inner);

  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) {
    print('Requête: ${request.method} ${request.url}');
    
    return _inner.send(request).then((response) {
      print('Réponse: ${response.statusCode}');
      return response;
    });
  }
}
```

## Endpoints GeoService courants

```
GET  /isochrone/api/locations              # Lister les emplacements
POST /isochrone/api/isochrones             # Créer une isochrone
GET  /isochrone/api/isochrones/{id}        # Obtenir une isochrone
PUT  /isochrone/api/isochrones/{id}        # Mettre à jour une isochrone
DELETE /isochrone/api/isochrones/{id}      # Supprimer une isochrone
GET  /isochrone/api/isochrones/{id}/export # Exporter une isochrone
```

## Voir aussi

- [Documentation de login](LOGIN_IMPLEMENTATION.md)
- [Guide complet de l'API](https://github.com/cunvoas/geoservice-isochrone)
