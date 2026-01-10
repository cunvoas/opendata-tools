import 'package:flutter/material.dart';
import '../screens/login_screen.dart';
import '../screens/home_screen.dart';

/// Configuration des routes de l'application
class AppRoutes {
  static const String login = '/login';
  static const String home = '/home';

  /// Obtient les routes de l'application
  static Map<String, WidgetBuilder> getRoutes() {
    return {
      login: (context) => const LoginScreen(
            serverFqdn: 'http://localhost:8090',
          ),
      home: (context) => const HomeScreen(),
    };
  }
}
