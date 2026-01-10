import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';

/// Écran d'accueil - affiché après une connexion réussie
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AuthProvider>(
      builder: (context, authProvider, child) {
        final user = authProvider.currentUser;

        if (!authProvider.isAuthenticated || user == null) {
          // Rediriger vers la page de login si non authentifié
          WidgetsBinding.instance.addPostFrameCallback((_) {
            Navigator.of(context).pushReplacementNamed('/login');
          });
          return const Scaffold(
            body: Center(
              child: CircularProgressIndicator(),
            ),
          );
        }

        return Scaffold(
          appBar: AppBar(
            title: const Text('GeoService Mobile'),
            elevation: 0,
            actions: [
              IconButton(
                icon: const Icon(Icons.logout),
                onPressed: () {
                  authProvider.logout();
                  Navigator.of(context).pushReplacementNamed('/login');
                },
              ),
            ],
          ),
          body: SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Bienvenue
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Theme.of(context).colorScheme.primaryContainer,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Bienvenue, ${user.fullName}!',
                        style: Theme.of(context).textTheme.headlineSmall,
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Vous êtes maintenant connecté',
                        style: Theme.of(context).textTheme.bodyMedium,
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 24),

                // Informations utilisateur
                Text(
                  'Informations de l\'utilisateur',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 12),
                _InfoCard(
                  label: 'ID Utilisateur',
                  value: user.userId.toString(),
                ),
                _InfoCard(
                  label: 'Nom d\'utilisateur',
                  value: user.username,
                ),
                _InfoCard(
                  label: 'Email',
                  value: user.email,
                ),
                _InfoCard(
                  label: 'Rôle',
                  value: user.role,
                ),
                const SizedBox(height: 24),

                // Informations token
                Text(
                  'Informations d\'authentification',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 12),
                _InfoCard(
                  label: 'Type de token',
                  value: user.tokenType,
                ),
                _InfoCard(
                  label: 'Expiration du token (secondes)',
                  value: user.expiresIn.toString(),
                ),
                const SizedBox(height: 24),

                // Bouton de renouvellement du token
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: () async {
                      final refreshed = await authProvider.refreshAccessToken();
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(
                              refreshed
                                  ? 'Token renouvelé avec succès'
                                  : 'Erreur lors du renouvellement du token',
                            ),
                            backgroundColor:
                                refreshed ? Colors.green : Colors.red,
                          ),
                        );
                      }
                    },
                    icon: const Icon(Icons.refresh),
                    label: const Text('Renouveler le token'),
                  ),
                ),
                const SizedBox(height: 12),

                // Bouton de déconnexion
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: () {
                      authProvider.logout();
                      Navigator.of(context).pushReplacementNamed('/login');
                    },
                    icon: const Icon(Icons.logout),
                    label: const Text('Se déconnecter'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

/// Widget pour afficher une paire clé-valeur
class _InfoCard extends StatelessWidget {
  final String label;
  final String value;

  const _InfoCard({
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              label,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w500,
                  ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                value,
                textAlign: TextAlign.right,
                style: Theme.of(context).textTheme.bodySmall,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
