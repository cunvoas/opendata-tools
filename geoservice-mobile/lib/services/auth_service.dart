import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:flutter/foundation.dart';
import '../models/auth_response.dart';
import '../models/login_request.dart';

/// Service d'authentification pour communiquer avec l'API backend
class AuthService {
  final String baseUrl;
  final String apiEndpoint = '/isochrone/api/auth';

  AuthService({
    this.baseUrl = 'http://localhost:8980',
  });

  /// Effectue une tentative de connexion
  /// 
  /// Envoie les identifiants au backend et récupère les tokens JWT
  /// 
  /// Lève une exception si la connexion échoue
  Future<AuthResponse> login({
    required String username,
    required String password,
  }) async {
    debugPrint('🔐 [AuthService] Tentative de connexion...');
    debugPrint('   URL: $baseUrl$apiEndpoint/login');
    debugPrint('   Username: $username');
    debugPrint('   Password: ********');
    
    final loginRequest = LoginRequest(
      username: username,
      password: password,
    );

    final url = Uri.parse('$baseUrl$apiEndpoint/login');
    debugPrint('   URL complète: $url');

    try {
      debugPrint('📤 [AuthService] Envoi de la requête POST...');
      final requestBody = jsonEncode(loginRequest.toJson());
      // Masquer le mot de passe dans les logs
      final passwordPattern = '"password":"';
      final passwordIndex = requestBody.indexOf(passwordPattern);
      String maskedBody = requestBody;
      if (passwordIndex != -1) {
        final startIndex = passwordIndex + passwordPattern.length;
        final endIndex = requestBody.indexOf('"', startIndex);
        if (endIndex != -1) {
          maskedBody = '${requestBody.substring(0, startIndex)}********${requestBody.substring(endIndex)}';
        }
      }
      debugPrint('   Body: $maskedBody');
      
      final response = await http.post(
        url,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: requestBody,
      );

      debugPrint('📥 [AuthService] Réponse reçue');
      debugPrint('   Status Code: ${response.statusCode}');
      debugPrint('   Headers: ${response.headers}');
      debugPrint('   Body: ${response.body}');

      if (response.statusCode == 200) {
        debugPrint('✅ [AuthService] Authentification réussie!');
        final jsonResponse = jsonDecode(response.body);
        return AuthResponse.fromJson(jsonResponse);
      } else if (response.statusCode == 401) {
        debugPrint('❌ [AuthService] Identifiants invalides (401)');
        throw AuthException('Identifiants invalides');
      } else {
        debugPrint('❌ [AuthService] Erreur HTTP: ${response.statusCode}');
        debugPrint('   Message: ${response.body}');
        throw AuthException(
          'Erreur d\'authentification: ${response.statusCode} - ${response.body}',
        );
      }
    } on http.ClientException catch (e) {
      debugPrint('❌ [AuthService] Erreur de connexion HTTP');
      debugPrint('   Exception: ${e.message}');
      throw AuthException(
        'Erreur de connexion: ${e.message}',
      );
    } catch (e, stackTrace) {
      debugPrint('❌ [AuthService] Erreur inattendue');
      debugPrint('   Exception: $e');
      debugPrint('   StackTrace: $stackTrace');
      throw AuthException('Erreur inattendue: $e');
    }
  }

  /// Renouvelle le token d'accès en utilisant le refresh token
  Future<AuthResponse> refreshToken(String refreshToken) async {
    debugPrint('🔄 [AuthService] Renouvellement du token...');
    final url = Uri.parse('$baseUrl$apiEndpoint/refresh');
    debugPrint('   URL: $url');

    try {
      final response = await http.post(
        url,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: jsonEncode({
          'refreshToken': refreshToken,
        }),
      );

      if (response.statusCode == 200) {
        final jsonResponse = jsonDecode(response.body);
        return AuthResponse.fromJson(jsonResponse);
      } else {
        throw AuthException(
          'Impossible de renouveler le token: ${response.statusCode}',
        );
      }
    } catch (e) {
      throw AuthException('Erreur lors du renouvellement du token: $e');
    }
  }

  /// Valide que le token d'accès est toujours valide
  Future<bool> validateToken(String accessToken) async {
    final url = Uri.parse('$baseUrl$apiEndpoint/validate');

    try {
      final response = await http.get(
        url,
        headers: {
          'Authorization': 'Bearer $accessToken',
          'Accept': 'application/json',
        },
      );

      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }
}

/// Exception personnalisée pour les erreurs d'authentification
class AuthException implements Exception {
  final String message;

  AuthException(this.message);

  @override
  String toString() => 'AuthException: $message';
}
