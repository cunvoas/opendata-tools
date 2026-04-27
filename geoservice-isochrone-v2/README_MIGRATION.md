# 🛠️ Migration Plan for geoservice-isochrone (Spring Boot 4 & Java 25)

## 🇫🇷 Contexte

Le projet **geoservice-isochrone** fournit des services d’analyse isochrone, basés sur Java 21+, Spring Boot 3.5, PostgreSQL/PostGIS, VueJS et Leaflet. L’objectif est de migrer l’ensemble du socle technique vers **Spring Boot 4** et **Java 25**, tout en assurant la continuité de service, la compatibilité des modules, et la sécurité.

## 🇬🇧 Context

The **geoservice-isochrone** project provides isochrone analysis services, based on Java 21+, Spring Boot 3.5, PostgreSQL/PostGIS, VueJS and Leaflet. The goal is to migrate the technical stack to **Spring Boot 4** and **Java 25**, ensuring service continuity, module compatibility, and security.

---

## 🎯 Objectifs / Objectives
- Compatibilité Spring Boot 4 & Java 25
- Non-régression fonctionnelle et technique
- Mise à jour des dépendances critiques (PostgreSQL, Hibernate, VueJS, etc.)
- Maintien de la CI/CD et de la documentation
- Sécurité et conformité

---

## 🪜 Étapes de migration / Migration Steps

1. **Audit initial**
   - Inventorier les modules, dépendances, plugins Maven, scripts Docker, CI/CD.
   - Identifier les points bloquants (API supprimées, dépendances obsolètes, etc.).

2. **Analyse de compatibilité**
   - Lancer le build avec Java 25 et Spring Boot 4 en mode dry-run.
   - Lister les erreurs de compilation, warnings, et incompatibilités majeures.

3. **Mise à jour des dépendances**
   - Adapter le `pom.xml` : versions Spring Boot, Hibernate, PostgreSQL, etc.
   - Mettre à jour les plugins Maven, Dockerfile, scripts CI/CD.

4. **Adaptation du code**
   - Corriger les usages d’API dépréciées ou supprimées.
   - Adapter la configuration Spring (application.yml/properties, beans, sécurité).
   - Mettre à jour les tests unitaires et d’intégration.

5. **Tests et validation**
   - Exécuter la suite de tests (unitaires, intégration, end-to-end).
   - Vérifier la couverture, la performance, la sécurité.
   - Recetter les modules front (VueJS/Thymeleaf) et back.

6. **Documentation & CI/CD**
   - Mettre à jour la documentation technique (README, HOWTO, diagrammes).
   - Adapter les workflows CI/CD (GitHub Actions, Jenkins, etc.).

7. **Bascule et monitoring**
   - Déployer sur un environnement de préproduction.
   - Mettre en place le monitoring (logs, métriques, alertes).
   - Basculer en production après validation.

---

## ⚠️ Points de vigilance / Key Points
- Incompatibilités majeures Spring Boot 4 (voir release notes)
- Migration Java 21 → 25 : modules supprimés, changements JVM
- Plugins Maven et dépendances tierces
- Scripts Docker et CI/CD
- Sécurité (vulnérabilités, dépendances critiques)

---

## 🤖 Modèle de prompt IA / AI Prompt Template

> "Tu es un agent expert chargé de migrer le projet geoservice-isochrone de Spring Boot 3.5/Java 21 vers Spring Boot 4/Java 25. Détaille chaque étape, liste les fichiers à modifier, les commandes à exécuter, les points de vigilance, et propose une checklist de validation."

> "You are an expert agent tasked with migrating the geoservice-isochrone project from Spring Boot 3.5/Java 21 to Spring Boot 4/Java 25. Detail each step, list files to modify, commands to run, key points, and provide a validation checklist."

---

## ✅ Checklist rapide / Quick Checklist
- [✅] Audit des dépendances et modules
- [✅] Build Java 21/Spring Boot 4 dry-run
- [✅] MAJ pom.xml, Dockerfile, scripts
- [✅] Correction du code et des tests
- [ ] Validation CI/CD
- [ ] Documentation à jour
- [ ] Monitoring post-migration

---

**Public cible / Target audience** : Développeur, DevOps, chef de projet, agent IA

