# TODO — Accessibilité numérique (RGAA 4.1.2)

Suivi des points d'accessibilité relevés en audit sur `geoservice-map-vue3`, et de ce qui
reste à faire. Périmètre : site `https://parcs-et-jardins.autmel.ovh`, application Vue 3.

Dernière mise à jour : 2026-11-21.

---

## 1. Fait

| Point | Fichier | Correction |
|---|---|---|
| Légende carte désynchronisée du mode daltonien | `geoservice-map-vue3/src/views/AppCarte.vue` | `colorblindMode` initialisé depuis `localStorage.getItem('colorblindMode') === 'true'`, comme `Isochrone.vue` et `AppNdvi.vue`. La légende sous la carte démarre donc avec les bonnes couleurs. Test ajouté dans `src/views/__tests__/AppCarte.test.js`. |
| Miniature absente avant le premier clic sur une vidéo | `geoservice-map-vue3/src/views/AppVideoAdmin.vue` | Attribut `:poster="poster"` ajouté sur `<video>`. Video.js lit l'attribut à l'initialisation. |
| Mode daltonien | `Isochrone.vue`, `StatsGraph.vue`, `AppNdvi.vue` | Déjà lu depuis `localStorage` à la création. `AppNdvi.vue` écoute aussi les événements `storage` et `colorblind-mode-changed` pour actualiser ses couleurs sans rechargement. |

---

## 2. À faire

### 2.1 Sous-titres ou transcription des vidéos (RGAA 4.1 / 4.3)

Les deux vidéos de la page `/video-admin` (`Saisir le contour du parc`, `Saisir les entrées
de parc`) n'ont ni piste de sous-titres ni transcription textuelle. Critères concernés :
RGAA 4.1 (sous-titres synchronisés) et 4.3 (transcription).

Prérequis : un fichier `.vtt` par vidéo (aucun dans le dépôt à ce jour). Le contenu parlé doit
être transcrit par une personne qui connaît la vidéo ; ne pas inventer.

Implémentation prévue :

1. Ajouter `subtitleSource` à chaque entrée du tableau `sources` de `AppVideoAdmin.vue`.
2. Ajouter dans le `<video>` (après le `<source>`, ligne ~32) :

   ```html
   <track kind="captions" srclang="fr" label="Français" :src="subtitleSource" default />
   ```

3. Dans `changeVideo()`, passer les pistes au lecteur pour que le changement de vidéo
   remplace aussi les sous-titres :

   ```js
   this.player.src({ src: this.videoSource, type: 'video/webm', tracks: [...] });
   ```

Variante acceptée : une transcription texte courte sous le lecteur (RGAA 4.3).

Statut : **bloqué** — en attente des fichiers `.vtt` / du texte de transcription.

---

## 3. Points de vigilance pour les prochains audits

- Toute nouvelle vue qui dépend du mode daltonien doit initialiser son état depuis
  `localStorage` (`colorblindMode`), sinon la carte et sa légende divergent au chargement.
- Tout média temporel ajouté doit arriver avec ses sous-titres ou sa transcription, plus le
  poster (`poster`), et idéalement une version audio-décrite si le contenu visuel est porteur.
- Vérifier le rendu final dans un navigateur : ces correctifs sont couverts par des tests
  unitaires mais pas par un test d'interface.
