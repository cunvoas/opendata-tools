/// Configuration de l'application
class AppConfig {
  /// URL de base du serveur API
  static const String defaultServerUrl = 'http://localhost:8980';
  
  /// Endpoint d'authentification
  static const String authEndpoint = '/isochrone/api/auth';
  
  /// Timeout pour les requêtes HTTP en secondes
  static const int requestTimeout = 30;
  
  /// Activer les logs de debug
  static const bool debugLogging = true;
}
