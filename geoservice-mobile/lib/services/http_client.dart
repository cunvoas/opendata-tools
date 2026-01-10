import 'package:http/http.dart' as http;
import '../providers/auth_provider.dart';

/// Client HTTP avec gestion automatique du token
class AuthenticatedHttpClient extends http.BaseClient {
  final http.Client _inner;
  final AuthProvider authProvider;

  AuthenticatedHttpClient({
    required this.authProvider,
    http.Client? innerClient,
  }) : _inner = innerClient ?? http.Client();

  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) {
    // Ajouter le token d'authentification si disponible
    if (authProvider.currentUser?.accessToken != null) {
      request.headers['Authorization'] =
          'Bearer ${authProvider.currentUser!.accessToken}';
    }

    // Ajouter les en-têtes par défaut
    request.headers['Accept'] = 'application/json';
    request.headers['Content-Type'] = 'application/json';

    return _inner.send(request);
  }
}

/// Extension pour faciliter l'utilisation des clients HTTP authentifiés
extension AuthHttpMethods on AuthenticatedHttpClient {
  /// GET authentifié
  Future<http.Response> getAuthenticated(
    Uri url, {
    Map<String, String>? headers,
  }) =>
      get(url, headers: headers);

  /// POST authentifié
  Future<http.Response> postAuthenticated(
    Uri url, {
    Map<String, String>? headers,
    Object? body,
  }) =>
      post(url, headers: headers, body: body);

  /// PUT authentifié
  Future<http.Response> putAuthenticated(
    Uri url, {
    Map<String, String>? headers,
    Object? body,
  }) =>
      put(url, headers: headers, body: body);

  /// DELETE authentifié
  Future<http.Response> deleteAuthenticated(
    Uri url, {
    Map<String, String>? headers,
  }) =>
      delete(url, headers: headers);
}
