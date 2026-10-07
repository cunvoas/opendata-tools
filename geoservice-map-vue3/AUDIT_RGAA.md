# Rapport d'audit accessibilité - Parcs & Jardins – MEL

> **Avertissement** : analyse statique du code source. Pas de certification, pas d'avis juridique. La note ci-dessous est un **indicateur de triage** ; le taux de conformité RGAA officiel exige un audit complet sur un échantillon de pages.

## 1. Contexte

- **Date** : 2026-10-07 - commit `0951128`
- **Réalisé par** : revue de code assistée (skill rgaa-eaa)
- **Périmètre** : `src`, `index.html` (21 fichiers analysés)
- **Référentiel** : RGAA 4.1.2 (WCAG 2.1) ; WCAG 2.2 (6 SC) et EAA rapportés hors RGAA (voir `references/wcag22-complement.md`)
- **Technologies détectées** : vue, tailwindcss, leaflet
- **Outils** : static-scan.mjs, extract-colors.mjs --check, audit.mjs
- **Environnement de test** : revue statique du code source (aucun navigateur, aucun lecteur d'écran)
- **Échantillon** : /carte ; /stats ; /information ; /aideoVideo

## 2. Accessibilité du site

### Note indicative : **63/100 - lettre D** (confiance MOYENNE)

> INDICATIF - revue statique du code, non officiel, ne vaut pas taux de conformité RGAA.

| Indicateur | Valeur |
|---|---|
| Couverture | 34/106 critères examinés (32.1 %) - confiance **MOYENNE** |
| Taux sur critères examinés (non officiel) | 44.1 % (critères examinés sans NC / critères examinés) |
| Taux officiel RGAA (validés / applicables) | il reste 49 critère(s) NT : taux officiel indisponible (comme dans la grille DINUM) |
| Grille officielle | C 13 - NC 19 - NA 25 - NT 49 (dont 2 « aucun défaut détecté par script » : ≠ conforme) |
| Actions | 19 à corriger (dont 3 P1 bloquant) - 8 à vérifier |
| Plafond | lettre C ramenée à D : au moins une action P1 (bloquant) |

Calcul : `note = 100 × (1 − Σ poids(NC) / critères examinés)` ; poids par gravité du critère NC : P1 1, P2 0.75, P3 0.5, P4 0.25. Lettres : A ≥ 90, B ≥ 75, C ≥ 60, D ≥ 40, E < 40. Un critère n'est « examiné » que si un défaut est démontré, si un verdict avec preuve est fourni, ou si l'élément visé existe dans le code et que le script n'a rien relevé. Les indices (À VÉRIFIER) ne sont **pas** comptés.

### Synthèse par thématique

| Thème | Critères | NC | C | NA | NT | dont À VÉRIFIER | dont sans défaut détecté |
|---|---|---|---|---|---|---|---|
| 1. Images | 9 | 1 | 2 | 5 | 1 | 0 | 0 |
| 2. Cadres | 2 | 0 | 0 | 2 | 0 | 0 | 0 |
| 3. Couleurs | 3 | 2 | 0 | 0 | 1 | 0 | 0 |
| 4. Multimédia | 13 | 0 | 0 | 1 | 12 | 1 | 0 |
| 5. Tableaux | 8 | 1 | 0 | 4 | 3 | 1 | 0 |
| 6. Liens | 2 | 1 | 1 | 0 | 0 | 0 | 0 |
| 7. Scripts | 5 | 2 | 0 | 1 | 2 | 1 | 0 |
| 8. Éléments obligatoires | 10 | 2 | 5 | 2 | 1 | 0 | 0 |
| 9. Structuration de l’information | 4 | 1 | 1 | 1 | 1 | 0 | 0 |
| 10. Présentation de l’information | 14 | 1 | 1 | 0 | 12 | 0 | 2 |
| 11. Formulaires | 13 | 2 | 2 | 1 | 8 | 0 | 0 |
| 12. Navigation | 11 | 5 | 1 | 1 | 4 | 0 | 0 |
| 13. Consultation | 12 | 1 | 0 | 7 | 4 | 1 | 0 |

## 3. Description des erreurs d'accessibilité

### 1. Images

_Donner à chaque image porteuse d'information une alternative pertinente ; ignorer les images décoratives._

- **A14** [NC] P3 - RGAA 1.5 - SVG de l'algorithme (diagramme) sans role="img" ni alternative textuelle alors qu'il est porteur d'information
  - 1 occurrence(s) : `src/views/AppInformation.vue:76`
- **A22** [À VÉRIFIER] P3 - RGAA 1.1, 7.1, 12.8 (WCAG 1.1.1 / 2.1.1) - map library detected: see references/cartes-et-graphiques.md (keyboard use, controls names, textual alternative; EAA excludes some online...
  - 2 occurrence(s) : `src/components/Isochrone.vue:12`, `src/main.js:6`
- **A25** [À VÉRIFIER] P4 - RGAA 1.1 (WCAG 1.1.1) - <svg> with no accessible name nor aria-hidden
  - 1 occurrence(s) : `src/views/AppInformation.vue:76`

### 2. Cadres

_Donner à chaque cadre un titre pertinent._

Aucun constat des scripts sur ce thème (≠ conformité ; voir grille).

### 3. Couleurs

_Ne pas donner l'information uniquement par la couleur ; contrastes suffisants._

- **A09** [NC] P2 - RGAA 3.2 - texte blanc sur fond vert à 3.67:1 (boutons Accès rapide) et 3.22:1 (boutons vidéo) ; version en text-xs text-gray-400 à 2.54:1 ; titre d...
  - 6 occurrence(s) : `src/components/Com2coQuickAccess.vue:11`, `src/views/AppVideoAdmin.vue:15`, `src/components/HeaderAsso.vue:4`, `src/App.vue:32`, `src/views/AppCarte.vue:126`, … (+1)
- **A13** [NC] P3 - RGAA 3.3 - pastilles de légende rendues avec opacity-40 sur fond blanc : several sous le seuil de 3:1 (ex. #57ee17@0.4 = 1.23:1, #b0b0e8@0.4 = 1.31:...
  - 2 occurrence(s) : `src/views/AppCarte.vue:18`, `src/components/Isochrone.vue:102`
- **A23** [À VÉRIFIER] P4 - RGAA 3.2 (WCAG 1.4.3) - paire non résolue statiquement (partner-unknown) - src/App.vue:19 [.dataDetail] ? sur rgba(255, 255, 255, 0.9)
  - 74 occurrence(s) : `src/App.vue:19`, `src/App.vue:28`, `src/App.vue:33`, `src/components/Autocomplete.vue:9`, `src/components/Autocomplete.vue:12`, … (+69)

> **Faux positif écarté** : `src/views/AppInformation.vue:47` (paires conditionnelles cohérentes : slate-600 sur slate-100 = 6,92:1 et blanc sur blue-900 = 10,36:1 — vérifié avec `contrast-check.mjs`). Seuls les 2 autres échecs de `extract-colors` sur ce thème sont confirmés.

### 4. Multimédia

_Transcriptions, sous-titres, audio-description ; médias contrôlables au clavier et à la souris._

- **A26** [À VÉRIFIER] P4 - RGAA 4.3 (WCAG 1.2.2) - <video> without <track kind="captions">
  - 1 occurrence(s) : `src/views/AppVideoAdmin.vue:24`

### 5. Tableaux

_Titres, en-têtes et liaisons corrects pour les tableaux de données ; linéarisation des tableaux de mise en forme._

- **A15** [NC] P3 - RGAA 5.6 - tableau de légende composé uniquement de <td> : aucun en-tête de ligne ni de colonne déclaré
  - 1 occurrence(s) : `src/views/AppCarte.vue:14`
- **A20** [À VÉRIFIER] P3 - RGAA 5.7 (WCAG 1.3.1) - <th> without scope/headers on a data table (needed for complex tables)
  - 1 occurrence(s) : `src/components/StatsTableAccessibility.vue:3`
### 6. Liens

_Intitulés de liens explicites, y compris hors contexte._

- **A06** [NC] P2 - RGAA 6.1 - lien « plein écran » dont l'intitulé est l'émoticône 🖥️ (le title= n'est pas repris comme nom accessible car le contenu texte existe)
  - 1 occurrence(s) : `src/components/Isochrone.vue:76`
- **A24** [À VÉRIFIER] P4 - RGAA 6.1 (WCAG 3.2.5 (AAA) / G201) - target="_blank" without announcing the new window. NOT a RGAA 13.2 failure by itself (13.2 targets windows opened WITHOUT user action; a ...
  - 11 occurrence(s) : `src/components/HeaderAsso.vue:15`, `src/components/HeaderAsso.vue:26`, `src/components/HeaderAsso.vue:37`, `src/components/HeaderAsso.vue:48`, `src/components/Isochrone.vue:411`, … (+6)

### 7. Scripts

_Scripts contrôlables au clavier et au pointeur, compatibles avec les technologies d'assistance, messages de statut._

- **A01** [NC] P1 - RGAA 7.3 - 5 cartes « Thésaurus » et les suggestions d'autocomplétion sont activées uniquement au clic : <div>/<li> sans role, sans tabindex, sans g...
  - 6 occurrence(s) : `src/components/Autocomplete.vue:13`, `src/views/AppInformation.vue:177`, `src/views/AppInformation.vue:181`, `src/views/AppInformation.vue:185`, `src/views/AppInformation.vue:189`, … (+1)
- **A16** [NC] P3 - RGAA 7.5 - message d'état « Chargement en cours... » rendu dans un <div> nu : non restitué par les technologies d'assistance (ni role="status", ni a...
  - 1 occurrence(s) : `src/components/StatsGraph.vue:16`

### 8. Éléments obligatoires

_Code source valide, titre de page pertinent, langue indiquée, balises non détournées._

- **A05** [NC] P2 - RGAA 8.2 - id="checkbox" dupliqué (cases Parcs et Isochrones rendues simultanément) : code non valide et référence de label ambiguë (les deux label ...
  - 2 occurrence(s) : `src/components/Isochrone.vue:17`, `src/components/Isochrone.vue:25`
- **A19** [NC] P4 - RGAA 8.9 - nom de la commune rendu avec <b id="villeId"> alors qu'il porte le titre du graphique (usage purement décoratif de <b>)
  - 1 occurrence(s) : `src/components/StatsGraph.vue:3`
### 9. Structuration de l’information

_Titres, listes et citations pour structurer l'information ; structure cohérente._

- **A11** [NC] P2 - RGAA 9.1 - pas de <h1> sur /carte, /stats ni /aideoVideo ; premier titre de page = <h3> (HeaderAsso) puis saut de niveau jusqu'à <h5> sur /aideoVide...
  - 4 occurrence(s) : `src/components/HeaderAsso.vue:10`, `src/views/AppVideoAdmin.vue:3`, `src/views/AppVideoAdmin.vue:22`, `src/views/AppCarte.vue:2`

### 10. Présentation de l’information

_Feuilles de styles, zoom 200 %, espacement, focus visible, contenus additionnels au survol/focus._

- **A07** [NC] P2 - RGAA 10.6 - lien dans un paragraphe sans soulignement ni couleur distincte : la préflight Tailwind hérite text-decoration:inherit (aucun soulignement...
  - 1 occurrence(s) : `src/views/AppInformation.vue:130`

### 11. Formulaires

_Champs associés à leur étiquette, groupes, boutons explicites, erreurs de saisie, finalité des champs._

- **A03** [NC] P2 - RGAA 11.9 - 5 boutons « × » de fermeture de modale sans intitulé exploitable (glyphe seul)
  - 5 occurrence(s) : `src/views/AppInformation.vue:203`, `src/views/AppInformation.vue:219`, `src/views/AppInformation.vue:235`, `src/views/AppInformation.vue:248`, `src/views/AppInformation.vue:261`
- **A10** [NC] P2 - RGAA 11.1 - 3 <select> de localisation sans aucune étiquette, champ d'adresse identifié uniquement par son placeholder, et l'étiquette « Isochrones »...
  - 5 occurrence(s) : `src/components/SearchLocation.vue:3`, `src/components/SearchLocation.vue:13`, `src/components/SearchLocation.vue:24`, `src/components/Autocomplete.vue:3`, `src/components/Isochrone.vue:19`
### 12. Navigation

_Systèmes de navigation multiples, liens d'évitement, ordre de tabulation, pas de piège clavier._

- **A02** [NC] P1 - RGAA 12.11 - toute l'information de la carte n'est accessible qu'à la souris : détails des carreaux écrits dans #dataDetail au survol (mouseover/mouse...
  - 4 occurrence(s) : `src/components/Isochrone.vue:889`, `src/components/Isochrone.vue:1079`, `src/components/Isochrone.vue:1044`, `src/components/Isochrone.vue:1159`
- **A04** [NC] P2 - RGAA 12.8 - attributs tabindex="1" à "4" : ordre de tabulation forcé, prioritaire sur tous les autres focusables de la page (menu, logos, carte), don...
  - 4 occurrence(s) : `src/components/SearchLocation.vue:6`, `src/components/SearchLocation.vue:17`, `src/components/SearchLocation.vue:28`, `src/components/SearchLocation.vue:41`
- **A08** [NC] P2 - RGAA 12.7 - aucun lien d'évitement : à chaque changement de page, le clavier reprend au menu et aux 4 logos
  - 1 occurrence(s) : `src/components/HeaderAsso.vue:62`
- **A12** [NC] P2 - RGAA 12.6 - seul <nav> est identifiable : pas de <header>, pas de <main> autour du <router-view>, pas de <footer> => la zone de contenu principal n'e...
  - 3 occurrence(s) : `src/components/HeaderAsso.vue:2`, `src/components/HeaderAsso.vue:62`, `src/components/HeaderAsso.vue:90`
- **A18** [NC] P3 - RGAA 12.1 - un seul système de navigation visible (menu de 4 liens) : pas de plan du site, pas de moteur de recherche, pas de fil d'Ariane (le Breadc...
  - 1 occurrence(s) : `src/components/HeaderAsso.vue:62`

### 13. Consultation

_Contrôle des délais, changements de contexte, contenus en mouvement, documents, gestes simples._

- **A17** [NC] P3 - RGAA 13.5 - émoticône 🖥️ utilisée comme contenu du lien plein écran, sans alternative restituée (le title= n'est pas pris comme nom car le contenu t...
  - 1 occurrence(s) : `src/components/Isochrone.vue:83`
- **A27** [À VÉRIFIER] P4 - RGAA 13.8 (WCAG 2.3.3) - animations/transitions with no prefers-reduced-motion query in this file
  - 1 occurrence(s) : `src/components/Isochrone.vue:32`

### Hors grille (critère non rattachable)

- **A21** [À VÉRIFIER] P3 - HTML injected from strings: its accessibility (labels, alt, headings) cannot be checked statically (`dynamic-html`)

## 4. Plan d'actions

### 4.1 À corriger (non-conformités démontrées)

| ID | Prio | Effort | Critère | Action | Occ. | Tags |
|---|---|---|---|---|---|---|
| A01 | P1 | M | 7.3 | remplacer le <div> cliquable par un <button> (ou ajouter role="button" tabindex="0" + @keydown.enter/@keydown.space) ; pour la liste d'autocomplétion, basculer sur <button> dans un rôle listbox/option géré au clavier | 6 | bloquant |
| A02 | P1 | L | 12.11 | proposer un équivalent hors carte (tableau/liste des parcs du territoire, déjà partiellement present via StatsTableAccessibility) et/ou rendre les entités atteignables au clavier (liste de sélection synchronisée avec la carte, focus sur couche) ; annoncer le contenu de #dataDetail dans une zone aria-live | 4 | bloquant |
| A03 | P2 | S | 11.9 | aria-label="Fermer la fenêtre" sur chaque bouton + aria-hidden="true" sur le glyphe | 5 | quick-win |
| A04 | P2 | S | 12.8 | supprimer les tabindex positifs (conserver tabindex="0"/"-1" uniquement) et laisser l'ordre natif du DOM | 4 | quick-win |
| A05 | P2 | S | 8.2 | id uniques (id="cbParcs" / id="cbIsochrones") avec label for correspondants | 2 | quick-win |
| A06 | P2 | S | 6.1 | remplacer le glyph par du texte (« Plein écran ») ou ajouter un <span class="sr-only">Plein écran</span> + aria-hidden sur l'émoticône | 1 | quick-win |
| A07 | P2 | S | 10.6 | ajouter underline (et couleur de lien) à ce <a>, ou mettre en forme tous les liens de texte du site de façon homogène | 1 | quick-win |
| A08 | P2 | S | 12.7 | ajouter en tête de page un lien « Aller au contenu principal » visible au focus, pointant vers #contenu | 1 | quick-win |
| A09 | P2 | M | 3.2 | assombrir les fonds verts (emerald-700/green-700 ou texte plus foncé), passer la version en text-gray-600, #777 → #767676 ou plus foncé, × en text-slate-600 | 6 |  |
| A10 | P2 | M | 11.1 | <label for> explicite (Région / Intercommunalité / Commune / Adresse) ou aria-label, et ids uniques pour les cases à cocher | 5 |  |
| A11 | P2 | M | 9.1 | titre de page en <h1> dans chaque vue (le bandeau passe en <p> ou le <h1> est unique par vue) et suppression des sauts de niveaux (h3 → h4/h5) | 4 |  |
| A12 | P2 | M | 12.6 | baliser en <header>/<main>/<footer> et déplacer <main> autour du <router-view> | 3 |  |
| A13 | P3 | S | 3.3 | retirer l'opacité des pastilles de légende (couleur pleine) ou ajouter un contour contrasté, et contrôler le rendu des polygones en navigateur | 2 |  |
| A14 | P3 | S | 1.5 | ajouter role="img" et aria-label (ou aria-hidden="true" si on considère que la liste ordonnée adjacente en tient lieu, auquel cas le SVG ne doit pas être le seul canal) | 1 |  |
| A15 | P3 | S | 5.6 | passer la 1re cellule de chaque ligne en <th scope="row">, ou si le tableau est purement de mise en forme lui ajouter role="presentation" | 1 |  |
| A16 | P3 | S | 7.5 | passer le conteneur en role="status" aria-live="polite" (le conteneur existant de StatsTableAccessibility utilise déjà aria-live="polite") | 1 |  |
| A17 | P3 | S | 13.5 | texte visible ou <span class="sr-only"> avec aria-hidden sur l'émoticône | 1 |  |
| A18 | P3 | M | 12.1 | ajouter un second système : plan du site, page « toutes les pages », ou fil d'Ariane visible | 1 |  |
| A19 | P4 | S | 8.9 | remplacer par <p class="font-semibold"> ou par un <h2>/<h3> adapté à la hiérarchie des titres | 1 |  |

#### A01 - P1 - 5 cartes « Thésaurus » et les suggestions d'autocomplétion sont activées uniquement au clic : <div>/<li> sans role, sans tabindex, sans g...

- **Critère** : RGAA 7.3 - **Effort** : M (≈ 0,5-2 j, composant/CSS)
- **Preuve** : vérifié manuellement : script static-scan.mjs règle clickable-non-interactive (6 occurrences) + lecture du template (vérifié)
- **Correction** : remplacer le <div> cliquable par un <button> (ou ajouter role="button" tabindex="0" + @keydown.enter/@keydown.space) ; pour la liste d'autocomplétion, basculer sur <button> dans un rôle listbox/option géré au clavier
- **Vérification** : parcours clavier seul (Tab, Maj+Tab, Entrée, Espace, Échap) + lecteur d'écran sur le composant
- **Occurrences** :
  - `src/components/Autocomplete.vue:13`
  - `src/views/AppInformation.vue:177`
  - `src/views/AppInformation.vue:181`
  - `src/views/AppInformation.vue:185`
  - `src/views/AppInformation.vue:189`
  - `src/views/AppInformation.vue:193`

#### A02 - P1 - toute l'information de la carte n'est accessible qu'à la souris : détails des carreaux écrits dans #dataDetail au survol (mouseover/mouse...

- **Critère** : RGAA 12.11 - **Effort** : L (> 2 j, refonte/comportement)
- **Preuve** : vérifié manuellement : lecture du code : handlers uniquement mouseover/mouseout/tooltip sans équivalent clavier (Isochrone.vue:946,1044,1085,1138,1159) ; aucune balise focusable sur les entités (vérifié)
- **Correction** : proposer un équivalent hors carte (tableau/liste des parcs du territoire, déjà partiellement present via StatsTableAccessibility) et/ou rendre les entités atteignables au clavier (liste de sélection synchronisée avec la carte, focus sur couche) ; annoncer le contenu de #dataDetail dans une zone aria-live
- **Vérification** : parcours clavier seul sur plusieurs pages : ordre logique, pas de piège, lien d'évitement
- **Occurrences** :
  - `src/components/Isochrone.vue:889`
  - `src/components/Isochrone.vue:1079`
  - `src/components/Isochrone.vue:1044`
  - `src/components/Isochrone.vue:1159`

#### A03 - P2 - 5 boutons « × » de fermeture de modale sans intitulé exploitable (glyphe seul)

- **Critère** : RGAA 11.9 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : script static-scan.mjs règle button-icon-only (5 occurrences) + lecture (vérifié)
- **Correction** : aria-label="Fermer la fenêtre" sur chaque bouton + aria-hidden="true" sur le glyphe
- **Vérification** : relancer audit.mjs ; lecteur d'écran : chaque champ annoncé avec son étiquette ; erreurs annoncées
- **Occurrences** :
  - `src/views/AppInformation.vue:203`
  - `src/views/AppInformation.vue:219`
  - `src/views/AppInformation.vue:235`
  - `src/views/AppInformation.vue:248`
  - `src/views/AppInformation.vue:261`

#### A04 - P2 - attributs tabindex="1" à "4" : ordre de tabulation forcé, prioritaire sur tous les autres focusables de la page (menu, logos, carte), don...

- **Critère** : RGAA 12.8 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : script static-scan.mjs règle tabindex-positive (4 occurrences) + lecture (vérifié)
- **Correction** : supprimer les tabindex positifs (conserver tabindex="0"/"-1" uniquement) et laisser l'ordre natif du DOM
- **Vérification** : parcours clavier seul sur plusieurs pages : ordre logique, pas de piège, lien d'évitement
- **Occurrences** :
  - `src/components/SearchLocation.vue:6`
  - `src/components/SearchLocation.vue:17`
  - `src/components/SearchLocation.vue:28`
  - `src/components/SearchLocation.vue:41`

#### A05 - P2 - id="checkbox" dupliqué (cases Parcs et Isochrones rendues simultanément) : code non valide et référence de label ambiguë (les deux label ...

- **Critère** : RGAA 8.2 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : script static-scan.mjs règle dup-id (Isochrone.vue:25, premier à la ligne 17) + lecture (aucune branche v-if/v-else exclusive) (vérifié)
- **Correction** : id uniques (id="cbParcs" / id="cbIsochrones") avec label for correspondants
- **Vérification** : relancer audit.mjs ; valider le document généré (validateur HTML) sur la page rendue
- **Occurrences** :
  - `src/components/Isochrone.vue:17`
  - `src/components/Isochrone.vue:25`

#### A06 - P2 - lien « plein écran » dont l'intitulé est l'émoticône 🖥️ (le title= n'est pas repris comme nom accessible car le contenu texte existe)

- **Critère** : RGAA 6.1 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : lecture du code Isochrone.vue:76-84 ; calcul du nom accessible selon l'algorithme (contenu texte prioritaire sur title)
- **Correction** : remplacer le glyph par du texte (« Plein écran ») ou ajouter un <span class="sr-only">Plein écran</span> + aria-hidden sur l'émoticône
- **Vérification** : relancer audit.mjs ; liste des liens du lecteur d'écran : intitulés compréhensibles hors contexte
- **Occurrences** :
  - `src/components/Isochrone.vue:76`

#### A07 - P2 - lien dans un paragraphe sans soulignement ni couleur distincte : la préflight Tailwind hérite text-decoration:inherit (aucun soulignement...

- **Critère** : RGAA 10.6 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : lecture du code AppInformation.vue:130-132 (aucune classe) + lecture de node_modules/tailwindcss/preflight.css:92-95 (a { text-decoration: inherit }) (vérifié)
- **Correction** : ajouter underline (et couleur de lien) à ce <a>, ou mettre en forme tous les liens de texte du site de façon homogène
- **Vérification** : navigateur : zoom 200 %, reflow 320 px, espacement du texte, focus visible
- **Occurrences** :
  - `src/views/AppInformation.vue:130`

#### A08 - P2 - aucun lien d'évitement : à chaque changement de page, le clavier reprend au menu et aux 4 logos

- **Critère** : RGAA 12.7 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : lecture du code : recherche d'un lien d'évitement/skip sans résultat dans src
- **Correction** : ajouter en tête de page un lien « Aller au contenu principal » visible au focus, pointant vers #contenu
- **Vérification** : parcours clavier seul sur plusieurs pages : ordre logique, pas de piège, lien d'évitement
- **Occurrences** :
  - `src/components/HeaderAsso.vue:62`

#### A09 - P2 - texte blanc sur fond vert à 3.67:1 (boutons Accès rapide) et 3.22:1 (boutons vidéo) ; version en text-xs text-gray-400 à 2.54:1 ; titre d...

- **Critère** : RGAA 3.2 - **Effort** : M (≈ 0,5-2 j, composant/CSS)
- **Preuve** : vérifié manuellement : calcul : extract-colors --check (3 FAIL) + contrast-check.mjs #777/#fff 4.48, #9ca3af/#fff 2.54, #94a3b8/#fff 2.56 (vérifié)
- **Correction** : assombrir les fonds verts (emerald-700/green-700 ou texte plus foncé), passer la version en text-gray-600, #777 → #767676 ou plus foncé, × en text-slate-600
- **Vérification** : recalculer chaque jeu de couleurs avec contrast-check.mjs ; vérifier le rendu réel (états, dégradés, images)
- **Occurrences** :
  - `src/components/Com2coQuickAccess.vue:11`
  - `src/views/AppVideoAdmin.vue:15`
  - `src/components/HeaderAsso.vue:4`
  - `src/App.vue:32`
  - `src/views/AppCarte.vue:126`
  - `src/views/AppInformation.vue:203`

#### A10 - P2 - 3 <select> de localisation sans aucune étiquette, champ d'adresse identifié uniquement par son placeholder, et l'étiquette « Isochrones »...

- **Critère** : RGAA 11.1 - **Effort** : M (≈ 0,5-2 j, composant/CSS)
- **Preuve** : vérifié manuellement : script static-scan.mjs règles field-no-label (4 occurrences) + lecture du template (vérifié)
- **Correction** : <label for> explicite (Région / Intercommunalité / Commune / Adresse) ou aria-label, et ids uniques pour les cases à cocher
- **Vérification** : relancer audit.mjs ; lecteur d'écran : chaque champ annoncé avec son étiquette ; erreurs annoncées
- **Occurrences** :
  - `src/components/SearchLocation.vue:3`
  - `src/components/SearchLocation.vue:13`
  - `src/components/SearchLocation.vue:24`
  - `src/components/Autocomplete.vue:3`
  - `src/components/Isochrone.vue:19`

#### A11 - P2 - pas de <h1> sur /carte, /stats ni /aideoVideo ; premier titre de page = <h3> (HeaderAsso) puis saut de niveau jusqu'à <h5> sur /aideoVide...

- **Critère** : RGAA 9.1 - **Effort** : M (≈ 0,5-2 j, composant/CSS)
- **Preuve** : vérifié manuellement : lecture des 4 vues + HeaderAsso (recherche <h1..h6> dans src)
- **Correction** : titre de page en <h1> dans chaque vue (le bandeau passe en <p> ou le <h1> est unique par vue) et suppression des sauts de niveaux (h3 → h4/h5)
- **Vérification** : plan des titres et repères via lecteur d'écran ou extension ; page assemblée
- **Occurrences** :
  - `src/components/HeaderAsso.vue:10`
  - `src/views/AppVideoAdmin.vue:3`
  - `src/views/AppVideoAdmin.vue:22`
  - `src/views/AppCarte.vue:2`

#### A12 - P2 - seul <nav> est identifiable : pas de <header>, pas de <main> autour du <router-view>, pas de <footer> => la zone de contenu principal n'e...

- **Critère** : RGAA 12.6 - **Effort** : M (≈ 0,5-2 j, composant/CSS)
- **Preuve** : vérifié manuellement : lecture du code : structure en <div> uniquement (HeaderAsso.vue:2,90) ; aucun <main>/<header>/<footer> dans src
- **Correction** : baliser en <header>/<main>/<footer> et déplacer <main> autour du <router-view>
- **Vérification** : parcours clavier seul sur plusieurs pages : ordre logique, pas de piège, lien d'évitement
- **Occurrences** :
  - `src/components/HeaderAsso.vue:2`
  - `src/components/HeaderAsso.vue:62`
  - `src/components/HeaderAsso.vue:90`

#### A13 - P3 - pastilles de légende rendues avec opacity-40 sur fond blanc : several sous le seuil de 3:1 (ex. #57ee17@0.4 = 1.23:1, #b0b0e8@0.4 = 1.31:...

- **Critère** : RGAA 3.3 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : calcul contrast-check.mjs avec --fg-opacity 0.4 sur les 5 couleurs de palette (échec vérifié)
- **Correction** : retirer l'opacité des pastilles de légende (couleur pleine) ou ajouter un contour contrasté, et contrôler le rendu des polygones en navigateur
- **Vérification** : recalculer chaque jeu de couleurs avec contrast-check.mjs ; vérifier le rendu réel (états, dégradés, images)
- **Occurrences** :
  - `src/views/AppCarte.vue:18`
  - `src/components/Isochrone.vue:102`

#### A14 - P3 - SVG de l'algorithme (diagramme) sans role="img" ni alternative textuelle alors qu'il est porteur d'information

- **Critère** : RGAA 1.5 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : lecture du code : <svg viewBox="0 0 600 560"> sans role/aria (AppInformation.vue:76) ; tests du critère 1.5 non satisfaits
- **Correction** : ajouter role="img" et aria-label (ou aria-hidden="true" si on considère que la liste ordonnée adjacente en tient lieu, auquel cas le SVG ne doit pas être le seul canal)
- **Vérification** : relancer audit.mjs ; lecteur d'écran : l'image est annoncée avec le bon nom (ou ignorée si décorative)
- **Occurrences** :
  - `src/views/AppInformation.vue:76`

#### A15 - P3 - tableau de légende composé uniquement de <td> : aucun en-tête de ligne ni de colonne déclaré

- **Critère** : RGAA 5.6 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : script static-scan.mjs règle table-no-label/table-no-th (src/views/AppCarte.vue:14) + lecture du template (aucun <th> sur ce tableau) (vérifié)
- **Correction** : passer la 1re cellule de chaque ligne en <th scope="row">, ou si le tableau est purement de mise en forme lui ajouter role="presentation"
- **Vérification** : lecteur d'écran : en-têtes de lignes/colonnes annoncés avec chaque cellule
- **Occurrences** :
  - `src/views/AppCarte.vue:14`

#### A16 - P3 - message d'état « Chargement en cours... » rendu dans un <div> nu : non restitué par les technologies d'assistance (ni role="status", ni a...

- **Critère** : RGAA 7.5 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : lecture du code StatsGraph.vue:16 (aucun role/aria-live) ; critère 7.5 non vérifié en technologie d'assistance
- **Correction** : passer le conteneur en role="status" aria-live="polite" (le conteneur existant de StatsTableAccessibility utilise déjà aria-live="polite")
- **Vérification** : parcours clavier seul (Tab, Maj+Tab, Entrée, Espace, Échap) + lecteur d'écran sur le composant
- **Occurrences** :
  - `src/components/StatsGraph.vue:16`

#### A17 - P3 - émoticône 🖥️ utilisée comme contenu du lien plein écran, sans alternative restituée (le title= n'est pas pris comme nom car le contenu t...

- **Critère** : RGAA 13.5 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : lecture du code Isochrone.vue:76-84 ; calcul du nom accessible (vérifié)
- **Correction** : texte visible ou <span class="sr-only"> avec aria-hidden sur l'émoticône
- **Vérification** : navigateur : délais, animations, nouvelles fenêtres, orientation, gestes
- **Occurrences** :
  - `src/components/Isochrone.vue:83`

#### A18 - P3 - un seul système de navigation visible (menu de 4 liens) : pas de plan du site, pas de moteur de recherche, pas de fil d'Ariane (le Breadc...

- **Critère** : RGAA 12.1 - **Effort** : M (≈ 0,5-2 j, composant/CSS)
- **Preuve** : vérifié manuellement : lecture du code : un seul <nav> (HeaderAsso.vue:62), recherche plan/sitemap/recherche sans objet dans src
- **Correction** : ajouter un second système : plan du site, page « toutes les pages », ou fil d'Ariane visible
- **Vérification** : parcours clavier seul sur plusieurs pages : ordre logique, pas de piège, lien d'évitement
- **Occurrences** :
  - `src/components/HeaderAsso.vue:62`

#### A19 - P4 - nom de la commune rendu avec <b id="villeId"> alors qu'il porte le titre du graphique (usage purement décoratif de <b>)

- **Critère** : RGAA 8.9 - **Effort** : S (< 1 h, attribut/texte)
- **Preuve** : vérifié manuellement : lecture du code StatsGraph.vue:3
- **Correction** : remplacer par <p class="font-semibold"> ou par un <h2>/<h3> adapté à la hiérarchie des titres
- **Vérification** : relancer audit.mjs ; valider le document généré (validateur HTML) sur la page rendue
- **Occurrences** :
  - `src/components/StatsGraph.vue:3`

### 4.2 À vérifier (indices, tests navigateur/lecteur d'écran requis)

| ID | Prio | Critère | Indice | Occ. |
|---|---|---|---|---|
| A20 | P3 | 5.7 | <th> without scope/headers on a data table (needed for complex tables) -> add scope="col"/"row" | 1 |
| A21 | P3 | - | HTML injected from strings: its accessibility (labels, alt, headings) cannot be checked statically -> review the generated markup; prefer templates/components | 1 |
| A22 | P3 | 1.1, 7.1, 12.8 | map library detected: see references/cartes-et-graphiques.md (keyboard use, controls names, textual alternative; EAA excludes some online... -> provide a non-map equivalent of the data | 2 |
| A23 | P4 | 3.2 | paire non résolue statiquement (partner-unknown) - src/App.vue:19 [.dataDetail] ? sur rgba(255, 255, 255, 0.9) -> vérifier le rendu réel (parent hérité, var() multi-thèmes, dégradé, image) | 74 |
| A24 | P4 | 6.1 | target="_blank" without announcing the new window. NOT a RGAA 13.2 failure by itself (13.2 targets windows opened WITHOUT user action; a ... -> add "(nouvelle fenêtre)" to the accessible name and rel="noopener noreferrer" | 11 |
| A25 | P4 | 1.1 | <svg> with no accessible name nor aria-hidden -> decorative: aria-hidden="true" focusable="false"; informative: role="img" + <title>/aria-label | 1 |
| A26 | P4 | 4.3 | <video> without <track kind="captions"> -> provide synchronized captions + transcript / audio description when relevant (RGAA 4.x) | 1 |
| A27 | P4 | 13.8 | animations/transitions with no prefers-reduced-motion query in this file -> honour prefers-reduced-motion (check it is handled globally) | 1 |

### 4.3 Points à vérifier (revue manuelle, non démontrés)

Tests manuels non exécutés pendant cette revue statique : ni non-conformité démontrée, ni conformité. Les points déjà listés en 4.2 (indices du scan) ne sont pas repris ici, sauf lorsqu'ils exigent un scénario de test précis.

| Critère | Point à vérifier | Où |
|---|---|---|
| 3.1 | Camembert : légende Chart.js désactivée (`legend.display: false`) et aucune étiquette de part => chaque part n'est identifiée que par sa couleur. Un équivalent textuel existe (`StatsTableAccessibility`, avec `<caption>` + `th scope="col"`), mais ce composant n'est **pas déclaré** dans l'objet `components` de `StatsGraph.vue` : Vue émet un avertissement de résolution et le rendu de ce tableau doit être contrôlé. | src/components/StatsGraph.vue:5,14,37 |
| 7.1 | Restitution par un lecteur d'écran des widgets dynamiques (autocomplétion, carte Leaflet, modales de thésaurus) et mémorisation de position après changement de route. | src/components/Autocomplete.vue, src/views/AppInformation.vue:199 |
| 10.7 | Prise de focus : `.input-field`/`.select-field` utilisent `outline-none` mais gardent `focus:ring-2 focus:ring-blue-500` (calculé 3,68:1 sur blanc, suffisant) ; les liens du menu, les boutons, le lien plein écran et les contrôles Leaflet n'ont pas de style `:focus-visible` dédié. | src/assets/tailwind.css:14,18 |
| 11.6 | Regroupements sans `<fieldset>/<legend>` : 4 listes de localisation, 5 cases de calques. | src/components/SearchLocation.vue:2, src/components/Isochrone.vue:2 |
| 11.13 | Finalité du champ adresse non déclarée (`autocomplete="street-address"`) : remplissage automatique impossible. | src/components/Autocomplete.vue:3 |
| 12.9 | Piège clavier possible sur la carte Leaflet (conteneur focusable) et sur le lecteur video.js : parcours Tab / Échap / Espace non exécuté. | src/components/Isochrone.vue:52, src/views/AppVideoAdmin.vue:24 |
| 13.9 | Orientation et reflow : carte imposée à 700 px de haut / 95 % de large ; comportement en portrait 320 px non testé (aucun verrouillage d'orientation détecté). | src/components/Isochrone.vue:58 |


## 5. Conclusion

### Priorisation des corrections (3 axes du modèle de rapport DINUM)

1. **Fonctionnalités et contenus essentiels** : à définir avec le responsable produit (parcours critiques : connexion, achat, formulaire clé). Corriger en premier les actions de ces parcours, quel que soit le critère.
2. **Critères bloquants (P1)** : A01 (7.3) et A02 (12.11) — même racine : aucun élément de la carte et du thésaurus n'est activable au clavier.
3. **Facilité de mise en œuvre (quick wins P1-P2, effort S)** : A03 (11.9), A04 (12.8), A05 (8.2), A06 (6.1), A07 (10.6), A08 (12.7).

### Avis

Revue statique : 34/106 critères examinés, 19 non conforme(s) démontré(s). Ce rapport ne permet pas de déclarer le service conforme ; il liste des défauts à corriger et des points à tester. Prochaines étapes : tests navigateur (clavier, zoom 200 %, reflow 320 px, lecteur d'écran), audit RGAA complet sur échantillon si une déclaration d'accessibilité est requise.

## Annexe - Grille d'audit (106 critères RGAA 4.1.2)

Statuts officiels : **C** conforme (preuve exigée), **NC** non conforme, **NA** non applicable (cas particulier cité), **NT** non testé.

| Critère | Thème | Statut | Examen | Source | Commentaire |
|---|---|---|---|---|---|
| 1.1 | 1. Images | **C** | vérifié (preuve fournie) | manuel | lecture : 4 <img> de logos avec alt non vide (src/components/HeaderAsso.vue:21,32,43,54) ; aucune autre balise <img> dans src (recherche globale) ⚠ 3 indice(s) script à lever |
| 1.2 | 1. Images | **NA** | non applicable | manuel | lecture : aucune image de décoration dans le contenu (favicon.ico hors corps de page, src/assets/location.png non référencé dans aucun template) |
| 1.3 | 1. Images | **C** | vérifié (preuve fournie) | manuel | lecture : alt = nom de l'association (Aut'MEL', Deûl'Air, Entrelianes, LM Oxygène), cohérent avec la destination helloasso du lien parent (HeaderAsso.vue:15-58) |
| 1.4 | 1. Images | **NA** | non applicable | manuel | lecture : aucun CAPTCHA ni image-test dans src |
| 1.5 | 1. Images | **NC** | défaut démontré | manuel+script | 1 occurrence(s) |
| 1.6 | 1. Images | **NA** | non applicable | manuel | lecture : aucune image nécessitant une description détaillée (logos + diagramme doublé d'une liste textuelle) |
| 1.7 | 1. Images | **NA** | non applicable | manuel | lecture : aucune description détaillée présente |
| 1.8 | 1. Images | **NT** | non couvert par les scripts | - |  |
| 1.9 | 1. Images | **NA** | non applicable | manuel | lecture : aucun élément <figure>/<figcaption> ni légende d'image dans src |
| 2.1 | 2. Cadres | **NA** | non applicable | manuel | lecture : aucun <iframe>, <object>, <embed> ni frame dans src et index.html (recherche globale) ; la carte Leaflet n'est pas un cadre |
| 2.2 | 2. Cadres | **NA** | non applicable | manuel | lecture : aucun cadre donc pas de titre de cadre à évaluer |
| 3.1 | 3. Couleurs | **NT** | non couvert par les scripts | - |  |
| 3.2 | 3. Couleurs | **NC** | défaut démontré | manuel+script | 9 occurrence(s) |
| 3.3 | 3. Couleurs | **NC** | défaut démontré | manuel+script | 2 occurrence(s) |
| 4.1 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.2 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.3 | 4. Multimédia | **NT** | À VÉRIFIER (indice script) | script | 1 indice(s) |
| 4.4 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.5 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.6 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.7 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.8 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.9 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.10 | 4. Multimédia | **NA** | non applicable | manuel | lecture : aucun attribut autoplay et aucun déclenchement de son automatique dans src |
| 4.11 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.12 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 4.13 | 4. Multimédia | **NT** | non couvert par les scripts | - |  |
| 5.1 | 5. Tableaux | **NA** | non applicable | manuel | lecture : aucun tableau de données complexe (2 tableaux simples max, sans colspan/rowspan structurels) |
| 5.2 | 5. Tableaux | **NA** | non applicable | manuel | lecture : aucun résumé de tableau, aucun tableau complexe |
| 5.3 | 5. Tableaux | **NT** | non couvert par les scripts | - |  |
| 5.4 | 5. Tableaux | **NA** | non applicable | manuel | lecture : le tableau de légende de AppCarte n'a pas de titre ; seul StatsTableAccessibility en a un (caption) |
| 5.5 | 5. Tableaux | **NA** | non applicable | manuel | lecture : idem, pas de titre de tableau à évaluer hors StatsTableAccessibility dont le caption est pertinent |
| 5.6 | 5. Tableaux | **NC** | défaut démontré | manuel+script | 1 occurrence(s) |
| 5.7 | 5. Tableaux | **NT** | À VÉRIFIER (indice script) | script | 1 indice(s) |
| 5.8 | 5. Tableaux | **NT** | non couvert par les scripts | - |  |
| 6.1 | 6. Liens | **NC** | défaut démontré | manuel+script | 1 occurrence(s) |
| 6.2 | 6. Liens | **C** | vérifié (preuve fournie) | manuel | lecture : tous les <a> ont un intitulé (alt des logos, « site github du projet », « licence AGPLv3 », « Code Source (AGPLv3) », « Documentation INSEE… », texte des router-link Carte/Statistiques/Information/Aide en vidéo, attribut title du lien plein écran) |
| 7.1 | 7. Scripts | **NT** | À VÉRIFIER (indice script) | script | 2 indice(s) |
| 7.2 | 7. Scripts | **NT** | non couvert par les scripts | - |  |
| 7.3 | 7. Scripts | **NC** | défaut démontré | manuel+script | 12 occurrence(s) |
| 7.4 | 7. Scripts | **NA** | non applicable | manuel | lecture : aucun changement de contexte déclenché par un script (pas de navigation, pas de changement de page à la saisie ; le re-filtrage des listes reste dans la page) |
| 7.5 | 7. Scripts | **NC** | défaut démontré | manuel+script | 1 occurrence(s) |
| 8.1 | 8. Éléments obligatoires | **C** | vérifié (preuve fournie) | manuel | lecture : index.html:1 <!doctype html> |
| 8.2 | 8. Éléments obligatoires | **NC** | défaut démontré | manuel+script | 2 occurrence(s) |
| 8.3 | 8. Éléments obligatoires | **C** | vérifié (preuve fournie) | manuel | lecture : index.html:2 <html lang="fr"> |
| 8.4 | 8. Éléments obligatoires | **C** | vérifié (preuve fournie) | manuel | lecture : lang="fr" cohérent avec les contenus rédigés en français |
| 8.5 | 8. Éléments obligatoires | **C** | vérifié (preuve fournie) | manuel | lecture : index.html:17 <title> descriptif + mise à jour par route dans src/main.js:80 (document.title = to.meta.title) |
| 8.6 | 8. Éléments obligatoires | **C** | vérifié (preuve fournie) | manuel | lecture : titres distincts et descriptifs par route (src/router/index.js:19,28,37,46) |
| 8.7 | 8. Éléments obligatoires | **NA** | non applicable | manuel | lecture : aucun changement de langue dans les contenus (noms propres et sigles exclus du champ) |
| 8.8 | 8. Éléments obligatoires | **NA** | non applicable | manuel | lecture : aucun changement de langue à valider |
| 8.9 | 8. Éléments obligatoires | **NC** | défaut démontré | manuel+script | 1 occurrence(s) |
| 8.10 | 8. Éléments obligatoires | **NT** | non couvert par les scripts | - |  |
| 9.1 | 9. Structuration de l’information | **NC** | défaut démontré | manuel+script | 4 occurrence(s) |
| 9.2 | 9. Structuration de l’information | **NT** | non couvert par les scripts | - |  |
| 9.3 | 9. Structuration de l’information | **C** | vérifié (preuve fournie) | manuel | lecture : listes construites en <ul>/<ol> + <li> imbriqués valides (Autocomplete.vue:11, AppInformation.vue:50-74, HeaderAsso.vue:62 nav) |
| 9.4 | 9. Structuration de l’information | **NA** | non applicable | manuel | lecture : aucun élément <blockquote>/<q>/<cite> ni citation à signaler |
| 10.1 | 10. Présentation de l’information | **C** | vérifié (preuve fournie) | manuel | lecture : présentation portée par Tailwind/sass (src/assets/tailwind.css, src/style.css) sans mise en forme par balise de structure abusive hors constat 8.9 |
| 10.2 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 10.3 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 10.4 | 10. Présentation de l’information | **NT** | aucun défaut détecté par script (≠ conforme) | script |  |
| 10.5 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 10.6 | 10. Présentation de l’information | **NC** | défaut démontré | manuel+script | 1 occurrence(s) |
| 10.7 | 10. Présentation de l’information | **NT** | aucun défaut détecté par script (≠ conforme) | script |  |
| 10.8 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 10.9 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 10.10 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 10.11 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 10.12 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 10.13 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 10.14 | 10. Présentation de l’information | **NT** | non couvert par les scripts | - |  |
| 11.1 | 11. Formulaires | **NC** | défaut démontré | manuel+script | 6 occurrence(s) |
| 11.2 | 11. Formulaires | **C** | vérifié (preuve fournie) | manuel | lecture : les étiquettes présentes nomment la fonction du champ (« Année », « \| Parcs », « \| Isochrones », « \| Carrés INSEE », « \| Cadastre », « \| Mode daltonien ») et sont accolées à leur champ (Isochrone.vue:3-46) ; le défaut d'association de « Isochrones » est compté sous 11.1 |
| 11.3 | 11. Formulaires | **NT** | non couvert par les scripts | - |  |
| 11.4 | 11. Formulaires | **C** | vérifié (preuve fournie) | manuel | lecture : chaque <label> précède immédiatement son champ (Isochrone.vue:3-46) ; pas d'étiquette détachée |
| 11.5 | 11. Formulaires | **NT** | non couvert par les scripts | - |  |
| 11.6 | 11. Formulaires | **NT** | non couvert par les scripts | - |  |
| 11.7 | 11. Formulaires | **NT** | non couvert par les scripts | - |  |
| 11.8 | 11. Formulaires | **NT** | non couvert par les scripts | - |  |
| 11.9 | 11. Formulaires | **NC** | défaut démontré | manuel+script | 10 occurrence(s) |
| 11.10 | 11. Formulaires | **NT** | non couvert par les scripts | - |  |
| 11.11 | 11. Formulaires | **NT** | non couvert par les scripts | - |  |
| 11.12 | 11. Formulaires | **NA** | non applicable | manuel | lecture : aucun formulaire qui modifie/supprime des données ni de validation avec conséquence financière/juridique (le formulaire n'a pas de champ de saisie soumis) |
| 11.13 | 11. Formulaires | **NT** | non couvert par les scripts | - |  |
| 12.1 | 12. Navigation | **NC** | défaut démontré | manuel+script | 1 occurrence(s) |
| 12.2 | 12. Navigation | **C** | vérifié (preuve fournie) | manuel | lecture : le menu est rendu au même emplacement sur toutes les routes (HeaderAsso.vue:62-87, au-dessus du <router-view>) |
| 12.3 | 12. Navigation | **NT** | non couvert par les scripts | - |  |
| 12.4 | 12. Navigation | **NT** | non couvert par les scripts | - |  |
| 12.5 | 12. Navigation | **NT** | non couvert par les scripts | - |  |
| 12.6 | 12. Navigation | **NC** | défaut démontré | manuel+script | 3 occurrence(s) |
| 12.7 | 12. Navigation | **NC** | défaut démontré | manuel+script | 1 occurrence(s) |
| 12.8 | 12. Navigation | **NC** | défaut démontré | manuel+script | 8 occurrence(s) |
| 12.9 | 12. Navigation | **NT** | non couvert par les scripts | - |  |
| 12.10 | 12. Navigation | **NA** | non applicable | manuel | lecture : aucun raccourci clavier à une seule touche dans src |
| 12.11 | 12. Navigation | **NC** | défaut démontré | manuel+script | 4 occurrence(s) |
| 13.1 | 13. Consultation | **NA** | non applicable | manuel | lecture : aucune limite de temps ni expiration de session dans l'application |
| 13.2 | 13. Consultation | **NA** | non applicable | manuel | lecture : aucune fenêtre ouverte automatiquement (toutes les cibles _blank sont sur action explicite de l'utilisateur) |
| 13.3 | 13. Consultation | **NA** | non applicable | manuel | lecture : aucun document bureautique en téléchargement dans src |
| 13.4 | 13. Consultation | **NA** | non applicable | manuel | lecture : aucun document bureautique |
| 13.5 | 13. Consultation | **NC** | défaut démontré | manuel+script | 1 occurrence(s) |
| 13.6 | 13. Consultation | **NT** | non couvert par les scripts | - |  |
| 13.7 | 13. Consultation | **NA** | non applicable | manuel | lecture : aucun flash ni effet de luminosité dans src (transitions Tailwind simples) |
| 13.8 | 13. Consultation | **NT** | À VÉRIFIER (indice script) | script | 1 indice(s) |
| 13.9 | 13. Consultation | **NT** | non couvert par les scripts | - |  |
| 13.10 | 13. Consultation | **NA** | non applicable | manuel | lecture : aucun geste complexe (pinch/rotation) n'est requis ; zoom molette/clavier de Leaflet |
| 13.11 | 13. Consultation | **NT** | non couvert par les scripts | - |  |
| 13.12 | 13. Consultation | **NA** | non applicable | manuel | lecture : aucune fonctionnalité pilotée par le mouvement de l'appareil |

---
Modèle de rapport et méthode : DINUM, kit d'audit RGAA (Licence Ouverte 2.0) - https://accessibilite.numerique.gouv.fr/ressources/kit-audit/
