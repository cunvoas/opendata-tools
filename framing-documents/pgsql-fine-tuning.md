
Le "Tune" du moteur PostgreSQL pour un profil Batch

Par défaut, PostgreSQL est configuré pour de petits serveurs web (beaucoup de petites lectures/écritures simultanées). Pour un outil de calcul par lots comme celui d'Aut'MEL, l'architecte DevSecOps modifie la configuration interne de postgresql.conf pour libérer la puissance du NUC :
• shared_buffers et work_mem : Alloués généreusement (par exemple 25 % à 50 % de la RAM du NUC) pour que les calculs d'isochrones et les jointures géographiques se fassent directement dans la mémoire vive ultra-rapide plutôt que d'écrire sur le disque.
• synchronous_commit = off : Une astuce classique en DevSecOps pour les gros imports de données. On indique à PostgreSQL de ne pas attendre la confirmation physique du disque à chaque ligne insérée. En cas de coupure de courant, on perd le calcul en cours, mais la vitesse d'écriture est multipliée par 5 ou 10.