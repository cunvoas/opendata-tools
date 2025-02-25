<template>
    <div id="appInformation" valign="top" align="left">
        
        <span style="border: 2px">
            <p>
            Cette application a pour vocation de recenser les différents parcs et jardins des villes et des regroupements de communes.
            <br />
            Les parcs sont intégrés et géolocalisés sur une carte interactive qui permet d'évaluer les populations qui peuvent en bénéficier.
            <br />
            Nous avons intégré les recommandation de l'OMS qui préconisent au minimum 10m²/habitant en zone urbaine et 25 m²/habitant en zone périurbaine, les surfaces conseillées sont de 12 et 45 m²/habitant selon la densité de population.
            De plus la distance pour accéder aux parcs est de 5 minutes à pieds (soit 300m) en ville et 20 minutes à la campagne (soit 1200m).
            <br />
            Pour chaque parc, un travail collaboratif de plusieurs assocations à permis de recenser les entrées de chaque parcs.<br />
            <br />
            La zone d'accessibilité isochrone est établie par l'intermédiaire du service de calcul d'isochrone de l'IGN. Les données de densité des communes, des populations, des données carroyées sont fournies par l'INSEE. Le cadastre provient du site opendata du gouvernement.
            <br />
            Certaines villes proposent également les données de parc via opendata et elles ont été intégrées.
            <br />
            </p>
        <span>
            L'algorithme de calcul de la zone d'accessibilité est le suivant :
            <ul>
                <li> Déterminer les carreaux d'une commune avec le cadastre</li>
                <li> Pour chaque carreau:</li>
                <ul>
                    <li> recherche des isochones d'accesibilité de parcs qui ont une intersection avec le carré</li>
                    <li> fusion des polygones des isochrones</li>
                    <li> déterminations de tous les carrés qui ont une intersection avec ce polygone d'accès</li>
                    <ol>
                        <li> Pour les carrés complètement recouvert par le polygone, toute la population est contabilisée</li>
                        <li> pour les autres carrés la polulation est estimée au prorata le la surface en recouvrement</li>
                        <li> la surface de tous les parcs accessibles est comptabilisée et la densité est calculée au vu de la population qui partage les parcs</li>
                    </ol>
                </ul>
            </ul>
            <br />
            Ce calcul est réalisé pour les années disponibles avec l'INSEE.
        
        </span>

        <span>
            Le programme est distribué en opensource, il a été réalisé par des bénévoles et des associations. Il est possible de contribuer à l'amélioration des données, des algorithmes et des fonctionnalités.
            <br />
            Pour cela, il suffit de se rendre sur le <a target="_blank" href="https://github.com/cunvoas/opendata-tools/issues">site github du projet</a> et de proposer des améliorations.
            <br />
            Le programme peut aussi être copié en respectant la <a target="_blank" href="https://www.gnu.org/licenses/gpl-3.0.fr.html">licence GPLv3</a>.
            <br />
            Pour plus d'information, vous pouvez consulter la documentation sur le site du projet.
        </span>

        </span>
    
    </div>
</template>

<script>

export default {
    name: "AppInformation",
    components: {
    },
};
</script>

<style>
#app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 10px;
}
table.legend td {
    color: #ffffff;
    font-size: 12px;
    padding: 2px;
}


</style>
