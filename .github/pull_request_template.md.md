### Obligatoire

> Chaque __Pull Request__ doit être créee en __Draft__ (Brouillon) pour éviter spams de notifications.

## Disclaimer

- Description: _une petite description ici_
- Problème n°: [JIRA OFR-XXX](https://github.com/cunvoas/opendata-tools/issues/#)

## Autres informations

- Informations importantes pour comprendre la PR (non obligatoire)

## TAGS de résumé

#### Quels sont les tags de _conventionnal_

- [ ] feat : nouvelle fonction
- [ ] fix: correction de bug
- [ ] security: correction de securité, application OWASP, ...
- [ ] refactor : refactorisation du code
- [ ] test : seulment un test qui évolute, gerkin
- [ ] docs: documentation
- [ ] chore: travail sur les libs, tache rébarbatives après audit, génération de code ...
- [ ] ci : travail sur la CI
- [ ] perf : optimisation et performance

#### extra tags

- [ ] BREAKING CHANGE : contrat cassé, changement d'architecture, version majeure ...

## Checklist perso avant une revue:

- [ ] Ma PR is clean versus les audits (SonarQuebe, Lint, OWASP, ZA Proxy, ..)
  - Bugs, Vulnérabilités, Securité, Code puant
  - Les modules dépendant sont OK
- [ ] J'ai fait l'auto-revue de mon propre code
- [ ] J'ai mis à jour la documentation (MarkDown)
  - J'ai commenté mon code, particulièrement le code complexe et dur à comprendre.
- [ ] J'ai testé mon code sur mon serveur de dev
- [ ] J'ai ajouté des tests qui pourvent que ma correction est correcte ou que ma nouvelle fonctionnalité est opérationelle.
  - Les tests,n nouveaux et existants fonctionnent localement.
- [ ] J'ai fait les changement corresponfant au C4 model, Layer 1 & 2 à minima

