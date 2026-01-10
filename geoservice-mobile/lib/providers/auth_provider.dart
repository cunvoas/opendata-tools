import 'package:flutter/foundation.dart';
import '../models/auth_response.dart';
import '../services/auth_service.dart';

void _log(String message) {
  debugPrint('🔒 [AuthProvider] $message');
}

/// Provider pour gérer l'état d'authentification de l'application
class AuthProvider extends ChangeNotifier {
  late final AuthService _authService;
  
  AuthResponse? _currentUser;
  bool _isLoading = false;
  String? _error;
  bool _isAuthenticated = false;

  AuthProvider({
    String baseUrl = 'http://localhost:8980',
  }) {
    _log('Initialisation du AuthProvider');
    _log('BaseURL: $baseUrl');
    _authService = AuthService(baseUrl: baseUrl);
  }

  // Getters
  AuthResponse? get currentUser => _currentUser;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _isAuthenticated;

  /// Effectue une tentative de connexion
  Future<bool> login({
    required String username,
    required String password,
  }) async {
    _log('Début du login pour: $username');
    _log('Password: ********');
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _log('Appel du service d\'authentification...');
      final response = await _authService.login(
        username: username,
        password: password,
      );

      _log('Réponse reçue du service');
      _log('Token reçu: ${response.accessToken.substring(0, 20)}...');
      _log('User ID: ${response.userId}');
      _log('Username: ${response.username}');
      
      _currentUser = response;
      _isAuthenticated = true;
      _error = null;

      _log('✅ Login réussi!');
      notifyListeners();
      return true;
    } catch (e) {
      _log('❌ Erreur lors du login: $e');
      _error = e.toString();
      _isAuthenticated = false;
      _currentUser = null;

      notifyListeners();
      return false;
    } finally {
      _isLoading = false;
      _log('Fin du processus de login');
      notifyListeners();
    }
  }

  /// Renouvelle le token d'accès
  Future<bool> refreshAccessToken() async {
    if (_currentUser?.refreshToken == null) {
      _error = 'Pas de refresh token disponible';
      notifyListeners();
      return false;
    }

    try {
      final response = await _authService.refreshToken(
        _currentUser!.refreshToken,
      );

      _currentUser = response;
      _error = null;

      notifyListeners();
      return true;
    } catch (e) {
      _error = e.toString();
      _isAuthenticated = false;
      _currentUser = null;

      notifyListeners();
      return false;
    }
  }

  /// Valide le token actuel
  Future<bool> validateCurrentToken() async {
    if (_currentUser?.accessToken == null) {
      return false;
    }

    return await _authService.validateToken(_currentUser!.accessToken);
  }

  /// Déconnecte l'utilisateur
  void logout() {
    _currentUser = null;
    _isAuthenticated = false;
    _error = null;
    notifyListeners();
  }

  /// Réinitialise l'erreur
  void clearError() {
    _error = null;
    notifyListeners();
  }
}
