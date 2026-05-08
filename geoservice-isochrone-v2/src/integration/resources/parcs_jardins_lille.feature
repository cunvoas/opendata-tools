Feature: Import des parcs et jardins de Lille

  Scenario: Importer et vérifier la présence d’un parc
    Given le fichier opendata des parcs et jardins de Lille est téléchargé et importé
    When je recherche un parc nommé "Jardin des Plantes"
    Then je trouve un parc avec ce nom dans la base
