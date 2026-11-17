# sur la home publique

 Depuis le printemps 2026, l'outil va plus loin : il sait aussi simuler l'ajout de parcs et en mesurer l'effet pour les habitants, en comparant la situation avant et après. Cette fonction n'est disponible que dans notre système de gestion.

Encore faut-il savoir où poser ces parcs. C'est un casse-tête : le nombre de combinaisons possibles est immense. L'outil s'appuie pour cela sur la recherche opérationnelle, la branche des mathématiques qui cherche la meilleure solution parmi un très grand nombre de possibilités, comme pour un emploi du temps ou une tournée de livraison. Parmi les méthodes disponibles :

    Les règles d'abord (programmation par contraintes) : on fixe les règles du jeu, comme la surface minimale d'un parc, et le logiciel élimine toutes les combinaisons qui les enfreignent, jusqu'à trouver une solution exacte.
    L'évolution (algorithme génétique) : comme dans la nature, les meilleures propositions survivent. L'ordinateur en fabrique un grand nombre, garde les plus convaincantes, les croise et les modifie légèrement, génération après génération. Il obtient de très bonnes solutions quand le problème est trop vaste pour un calcul exact, sans garantie que ce soit la meilleure.
    Pas à pas (algorithme glouton) : on traite les secteurs de 200 m sur 200 m un par un, en commençant par celui où le manque de parc est le plus fort, puis on mesure l'effet sur ses voisins. C'est très rapide, mais pas toujours optimal : à chaque étape, on prend ce qui semble le mieux sur le moment.

Autre nouveauté, ajoutée pendant l'été 2026 : un mode manuel. Plutôt que de laisser l'ordinateur proposer des emplacements, on dessine soi-même un futur parc sur la carte, d'un simple cercle ou d'un contour précis.

Un clic sur « Évaluer », et la carte se colore, carreau de 200 m par carreau. Chacun affiche, avec sa population, les m² de parc par habitant et le manque de surface qui subsiste, avant et après l'ajout. 