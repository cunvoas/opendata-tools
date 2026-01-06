import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';

/// Écran de connexion avec formulaire de login
class LoginScreen extends StatefulWidget {
  final String? serverFqdn;

  const LoginScreen({
    super.key,
    this.serverFqdn,
  });

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  late final TextEditingController _usernameController;
  late final TextEditingController _passwordController;
  late final TextEditingController _serverController;
  final _formKey = GlobalKey<FormState>();
  bool _showPassword = false;

  @override
  void initState() {
    super.initState();
    _usernameController = TextEditingController();
    _passwordController = TextEditingController();
    _serverController = TextEditingController(
      text: widget.serverFqdn ?? 'http://localhost:8980',
    );
  }

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    _serverController.dispose();
    super.dispose();
  }

  void _handleLogin(AuthProvider authProvider) async {
    if (_formKey.currentState!.validate()) {
      debugPrint('🔑 [LoginScreen] Début de la connexion');
      debugPrint('   Server: ${_serverController.text}');
      debugPrint('   Username: ${_usernameController.text.trim()}');
      debugPrint('   Password: ********');
      
      // Mettre à jour l'URL du serveur dans le provider si nécessaire
      final newBaseUrl = _serverController.text.trim();
      debugPrint('   Recréation du AuthProvider avec baseUrl: $newBaseUrl');
      
      final success = await authProvider.login(
        username: _usernameController.text.trim(),
        password: _passwordController.text,
      );

      if (!mounted) return;

      if (success) {
        debugPrint('✅ [LoginScreen] Connexion réussie!');
        // Redirection vers l'écran principal après connexion réussie
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Bienvenue ${authProvider.currentUser?.fullName}',
            ),
            backgroundColor: Colors.green,
          ),
        );
        // Vous pouvez rediriger vers un autre écran ici
        // Navigator.of(context).pushReplacementNamed('/home');
      } else {
        debugPrint('❌ [LoginScreen] Connexion échouée');
        debugPrint('   Erreur: ${authProvider.error}');
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(authProvider.error ?? 'Erreur de connexion'),
            backgroundColor: Colors.red,
            duration: const Duration(seconds: 5),
          ),
        );
      }
    } else {
      debugPrint('⚠️ [LoginScreen] Validation du formulaire échouée');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<AuthProvider>(
      builder: (context, authProvider, child) {
        return Scaffold(
          appBar: AppBar(
            title: const Text('Connexion'),
            elevation: 0,
          ),
          body: SingleChildScrollView(
            padding: const EdgeInsets.all(24.0),
            child: Form(
              key: _formKey,
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Logo ou titre
                  const SizedBox(height: 40),
                  Icon(
                    Icons.location_on,
                    size: 80,
                    color: Theme.of(context).primaryColor,
                  ),
                  const SizedBox(height: 24),
                  Text(
                    'GeoService Mobile',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 40),

                  // Champ serveur (optionnel)
                  TextFormField(
                    controller: _serverController,
                    decoration: InputDecoration(
                      labelText: 'Serveur',
                      hintText: 'https://example.com',
                      prefixIcon: const Icon(Icons.storage),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                    validator: (value) {
                      if (value?.isEmpty ?? true) {
                        return 'Veuillez entrer l\'adresse du serveur';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),

                  // Champ nom d'utilisateur
                  TextFormField(
                    controller: _usernameController,
                    decoration: InputDecoration(
                      labelText: 'Nom d\'utilisateur',
                      hintText: 'john.doe',
                      prefixIcon: const Icon(Icons.person),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                    validator: (value) {
                      if (value?.isEmpty ?? true) {
                        return 'Veuillez entrer votre nom d\'utilisateur';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),

                  // Champ mot de passe
                  TextFormField(
                    controller: _passwordController,
                    obscureText: !_showPassword,
                    decoration: InputDecoration(
                      labelText: 'Mot de passe',
                      hintText: '••••••••',
                      prefixIcon: const Icon(Icons.lock),
                      suffixIcon: IconButton(
                        icon: Icon(
                          _showPassword
                              ? Icons.visibility
                              : Icons.visibility_off,
                        ),
                        onPressed: () {
                          setState(() {
                            _showPassword = !_showPassword;
                          });
                        },
                      ),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                    validator: (value) {
                      if (value?.isEmpty ?? true) {
                        return 'Veuillez entrer votre mot de passe';
                      }
                      if (value!.length < 6) {
                        return 'Le mot de passe doit contenir au moins 6 caractères';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 24),

                  // Message d'erreur
                  if (authProvider.error != null)
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.red.shade50,
                        border: Border.all(color: Colors.red.shade200),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        authProvider.error!,
                        style: TextStyle(
                          color: Colors.red.shade700,
                          fontSize: 12,
                        ),
                      ),
                    ),
                  if (authProvider.error != null) const SizedBox(height: 16),

                  // Bouton de connexion
                  ElevatedButton.icon(
                    onPressed: authProvider.isLoading
                        ? null
                        : () => _handleLogin(authProvider),
                    icon: authProvider.isLoading
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                            ),
                          )
                        : const Icon(Icons.login),
                    label: Text(
                      authProvider.isLoading
                          ? 'Connexion en cours...'
                          : 'Se connecter',
                    ),
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Lien d'aide (optionnel)
                  Center(
                    child: TextButton(
                      onPressed: () {
                        // Afficher un dialogue d'aide
                        showDialog(
                          context: context,
                          builder: (context) => AlertDialog(
                            title: const Text('Aide'),
                            content: const SingleChildScrollView(
                              child: Column(
                                crossAxisAlignment:
                                    CrossAxisAlignment.start,
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Text(
                                    'Adresse du serveur:',
                                    style: TextStyle(
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  SizedBox(height: 8),
                                  Text(
                                    'Entrez l\'adresse complète du serveur GeoService (ex: https://server.example.com)',
                                  ),
                                  SizedBox(height: 16),
                                  Text(
                                    'Identifiants:',
                                    style: TextStyle(
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  SizedBox(height: 8),
                                  Text(
                                    'Utilisez vos identifiants GeoService pour vous connecter.',
                                  ),
                                ],
                              ),
                            ),
                            actions: [
                              TextButton(
                                onPressed: () => Navigator.pop(context),
                                child: const Text('Fermer'),
                              ),
                            ],
                          ),
                        );
                      },
                      child: const Text('Besoin d\'aide ?'),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
