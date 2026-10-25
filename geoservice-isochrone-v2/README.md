# geoservice-isochrone


## Données sources

   - <img alt="logo" src="https://www.geoportail.gouv.fr/assets/images/logo-geoportail.svg" /> 
[Site GéoPortail](https://www.geoportail.gouv.fr/carte) 
   - <img alt="logo" src="https://www.insee.fr/static/img/logo_com_externe_semi_bold.png" height="40" /> 
[Site INSEE : Données carroyées à 200m ](https://www.insee.fr/fr/statistiques/6215138?sommaire=6215217)


## Indice NDVI des parcs (Copernicus CDSE)

Le calcul des indicateurs OMS enrichit chaque parc (`ParkAreaComputed`) avec des
statistiques de végétation **NDVI** : moyenne, minimum, maximum, écart-type et date
de fin de fenêtre d'acquisition.

- **Source** : Sentinel-2 **L2A** (`sentinel-2-l2a`), mosaïque `leastCC`
  (image la moins nuageuse) et masque `SCL` (nuages, ombres, eau exclus).
- **Zéro téléchargement d'image** : appel de l'API *Statistical* de Copernicus
  Data Space Ecosystem (`/statistics/v1`). Le serveur calcule les statistiques et
  ne renvoie que quelques centaines d'octets.
- **Fenêtre glissante sur l'année précédente**, configurable au format `MM-dd`
  (bornes incluses). Une seconde fenêtre de secours est utilisée uniquement si la
  première ne renvoie aucune donnée exploitable.
- **Non bloquant** : toute erreur (configuration, réseau, quota, timeout) est
  absorbée → colonnes NDVI nulles + log `WARN`, le calcul OMS continue.
- **Rafraîchissement** : au plus **un appel par parc et par an**, uniquement **à partir
  de septembre** (date mémorisée dans `ndvi_request_date`). Le rattrapage s'applique
  aussi aux lignes OMS déjà à jour, sans modifier leurs indicateurs ni leur date `updated`.

### Configuration

```yaml
application:
  ndvi:
    enabled: false                  # activer après création d'un OAuth client CDSE
    monthly-quota: 10000            # quota gratuit : 10 000 requêtes statistiques/mois
    timeout-ms: 10000
    window:
      from: "06-01"
      to: "06-07"
      fallback:                     # vide = pas de secours
        from: "06-08"
        to: "07-14"
    copernicus:
      client-id: ${CDSE_CLIENT_ID}
      client-secret: ${CDSE_CLIENT_SECRET}
      token-url: https://identity.dataspace.copernicus.eu/auth/realms/CDSE/protocol/openid-connect/token
      statistics-url: https://sh.dataspace.copernicus.eu/statistics/v1
```

Les identifiants proviennent du [Dashboard CDSE](https://dataspace.copernicus.eu/dashboard)
(*OAuth Clients → Create*). Détails OAuth et dépannage :
[`docs/copernicus-ndvi-oauth.md`](docs/copernicus-ndvi-oauth.md).

### Quota et débit

Limites CDSE du compte gratuit (Sentinel Hub APIs, reset le 1er du mois) :

| Ressource | Limite |
|---|---|
| Requêtes / mois | 10 000 |
| Requêtes / minute | 300 |
| Processing Units (PU) / mois | 10 000 |
| PU / minute | 300 |

- Compteur mensuel **persisté en base** (`ndvi_api_quota`, migration
  `v2.1.4_ndvi_api_quota.sql`) : réservation atomique, plafond garanti même après
  redémarrage ou en concurrence.
- Une unité est réservée avant l'envoi puis **remboursée si la réponse n'est pas 2XX** :
  CDSE ne facture que les requêtes réussies. Le token OAuth n'est pas un appel
  Sentinel Hub et n'est **pas** décompté.
- Le compteur local suit les **requêtes** ; le dashboard CDSE suit aussi les **PU**.
  1 requête Statistical coûte au minimum 0,01 PU et dépend de la taille, des bandes
  et des samples : les deux chiffres ne sont donc pas identiques.
- La taille de sortie est calculée à **10 m natif** (plafond 512×512) au lieu d'un
  suréchantillonnage 512×512 : les PU restent proportionnés à la surface du parc.
- Débit limité à **2 requêtes/seconde** (≤ 240 PU/min avec 2 PU max par requête),
  avec reprise sur `429` (`retry-after`).

### Activation et test réel

1. Renseigner `application.ndvi.enabled: true` et les identifiants (env/profil local,
   jamais en clair dans le repo).
2. Appliquer la migration quota si Flyway n'est pas utilisé sur la base.
3. Test d'appel réel (désactivé par défaut) :
   `TestClientCopernicusNdviLive` sur le parc de la Citadelle (Lille) — retirer
   `@Disabled` pour l'exécuter. Il consomme 1 requête du quota.

## Recommandation de l'OMS

 - [URBANISME ET SANTE](https://www.euro.who.int/__data/assets/pdf_file/0010/102106/E93982.pdf)
 - [Analyse préfecture](https://rstudio-pubs-static.s3.amazonaws.com/856429_6a6df33450a44c87b5d23638421c13a0.html#carte-r%C3%A9gionale-des-communes)

## Remerciements : 

### Pour les documentations

 - [OMS](https://www.who.int/fr)
 - [Institut national de l’information géographique et forestière (IGN)](https://www.ign.fr/)
 - [Commission Européenne](https://inspire.ec.europa.eu/)
 
### Pour les données

 - [GéoPortail](https://www.geoportail.gouv.fr)
 - [Cadastre](https://cadastre.data.gouv.fr)
 - [INSEE](https://www.insee.fr) 
 - [Métropole Européenne de Lille](https://www.lillemetropole.fr/)
 - [Ville de Roubaix](https://www.roubaix.fr)
 - [Ville de Tourcoing](https://www.tourcoing.fr)
 - [Ville de Villeneuve d'Ascq](https://www.villeneuvedascq.fr/)
 
### Pour le socle technique

 - [OpenStreetMap](https://www.openstreetmap.org/)
 - [OpenJDK](https://openjdk.org/), [SpringBoot](), [LocationTech](https://github.com/locationtech)
 - [PostgreSQL](https://www.postgresql.org/), [PostGIS](https://postgis.net/)
 - [VueJS](https://vuejs.org/), [Leaflet](https://leafletjs.com/), [Vue Leaflet](https://vue2-leaflet.netlify.app/)

  Plus de détails technique [ici](HOWTO.md).
 
 