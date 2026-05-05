# Flyway

   Ce répertoire contient les scripts SQL liés à l'évolution de l'application.
   
## Utilisation
   mvn flyway:migrate

Le plugin utilisera les variables d’environnement ou les propriétés Maven pour DB_URL, DB_USER et DB_PASSWORD, et appliquera les scripts présents dans src/main/resources/db/migration.