# Module `adress`

Ce module fournit des services pour la recherche et le traitement d'adresses à partir de l'API publique française [adresse.data.gouv.fr](https://adresse.data.gouv.fr/api-doc/adresse). Il permet de rechercher des adresses à partir d'une requête et d'un code INSEE, puis de parser les réponses GeoJSON pour les exploiter dans l'application.

## Structure
- **AdresseClientService** : Interface de service pour la recherche d'adresses.
- **AdresseClientServiceGouvImpl** : Implémentation du service utilisant l'API adresse.data.gouv.fr.
- **AdressGeoJsonParser** : Parseur des réponses GeoJSON en objets métier.
- **dto/** : Contient les objets de transfert de données (DTO) pour les adresses.

## Utilisation
1. Utilisez `AdresseClientService` pour effectuer une recherche d'adresse.
2. L'implémentation `AdresseClientServiceGouvImpl` interroge l'API et utilise `AdressGeoJsonParser` pour transformer la réponse en objets `AdressBo`.
3. Les DTO du sous-dossier `dto` servent à la manipulation des données d'adresses et à la désérialisation des réponses GeoJSON.

## Dépendances principales
- Spring (injection de dépendances)
- OkHttp (requêtes HTTP)
- Jackson (traitement JSON)
- JTS (géométrie)

## Exemple d'appel
```java
Set<AdressBo> adresses = adresseClientService.getAdresses("80021", "8 Boulevard du Port");
```

## Auteur
Projet open data, inspiré par les besoins de géolocalisation et d'analyse d'accessibilité.
