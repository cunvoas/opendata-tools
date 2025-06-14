<template>
    <div id="appInformation" valign="top" align="left">


        <span style="border: 2px">

            <h1>Information sur l'application</h1>
            <h2>Objectif</h2>
            <p>
                L'objectif de cette application est de permettre aux habitants des villes et des regroupements de
                communes
                de connaitre les parcs et jardins qui sont accessibles à pieds.
                <br />
                Elle permet de visualiser sur la carte et de connaitre la population qui partage les parcs ainsi que la
                densité de population.
            </p>

            <h2>Parcs et Jardins</h2>
            <p>
                Les parcs et jardins publiques sont des espaces verts accessibles qui permettent de se détendre, de se
                promener, de jouer, de se reposer, de pique-niquer, de lire, de se rencontrer,de s’aérer...
                <br />
                Les villes comptabilisent certains espaces comme des parcs et jardins, comme les cimetières, les
                bosquets, des places minéralisées. L'application les exclus des surfaces mais les noms sont disposibles
                dans les détails avec une croix.
            </p>
            <p>
                Ils sont intégrés et géolocalisés sur une carte interactive qui permet d'évaluer les populations qui
                peuvent en bénéficier.
                <br />
                Nous avons intégré les recommandation de l'OMS qui préconisent au minimum 10m²/habitant en zone urbaine
                et 25 m²/habitant en zone périurbaine, les surfaces conseillées sont respectivement de 12 et 45
                m²/habitant.
                De plus la distance pour accéder aux parcs est de 5 minutes à pieds (soit 300m) en ville et 20 minutes à
                la campagne (soit 1200m).
                <br />


                <br />
                La zone d'accessibilité isochrone est établie par l'intermédiaire du service de calcul d'isochrone de
                l'IGN. Les données de densité des communes, des populations, des données carroyées sont fournies par
                l'INSEE. Le cadastre provient du site opendata du gouvernement.
                <br />
                Certaines villes proposent également les données de parc via opendata et elles ont été intégrées.
                <br />
            </p>
            <h2>Les données utilisées et l'algorithme</h2>
            <p>
                Nous utilisons les données disponibles en OpenData lorsque cela est possible auquel s'ajoute un travail
                collaboratif de plusieurs assocations à permis de recenser les entrées de chaque parcs.<br />
                Pour chaque entrée, nous déterminons la zone d'accessibilité "isochrone" grace au service de calcul
                d'isochrone de l'IGN.<br />
                Les données de densité des communes, des populations, des données carroyées sont fournies par l'INSEE.
            </p>
            <p>
                L'algorithme de calcul de la zone d'accessibilité est le suivant :
            </p>
            <ul>
                <li> Déterminer les carreaux d'une commune avec le cadastre</li>
                <li> Pour chaque carreau:</li>
                <ul>
                    <li> recherche des isochones d'accesibilité de parcs qui ont une intersection avec le carré</li>
                    <li> fusion des polygones des isochrones</li>
                    <li> déterminations de tous les carrés qui ont une intersection avec ce polygone d'accès</li>
                    <ol>
                        <li> Pour les carrés complètement recouvert par le polygone, toute la population est
                            contabilisée</li>
                        <li> pour les autres carrés la polulation est estimée au prorata le la surface en recouvrement
                        </li>
                        <li> la surface de tous les parcs accessibles est comptabilisée et la densité est calculée au vu
                            de la population qui partage les parcs</li>
                    </ol>
                </ul>
            </ul>
            <br />
            <p>
                Ce calcul est réalisé pour les années disponibles avec l'INSEE.
            </p>

            <h2>Code source et contribution à l'application</h2>
            <p>
                Le programme est disponible en opensource, il a été réalisé par des bénévoles et des associations. Il
                est
                possible de contribuer à l'amélioration des données, des algorithmes et des fonctionnalités.
                <br />
                Pour cela, il suffit de se rendre sur le <a target="_blank" rel="noopener noreferrer"
                    href="https://github.com/cunvoas/opendata-tools/issues">site github du projet</a> et de proposer des
                améliorations.
                <br />
                Le programme peut aussi être copié en respectant la <a target="_blank" rel="noopener noreferrer"
                    href="https://www.gnu.org/licenses/gpl-3.0.fr.html">licence GPLv3</a>.
                <br />
                Pour plus d'information, vous pouvez consulter la documentation sur le site du projet.
            </p>

            <h2>Contribution à la saisie</h2>
            <p>
                Les contributions aux données sont réalisées sur une application dédiée. Pour y accéder, il faut en
                faire la
                demande via <a href="javascript:void(0);" @click="sendRequest">email</a>
                <br />
                Nous limitons le nombres de contributeur par territoire afin de garantir la qualité des données et la
                stabilité de notre seurveur.
                <br />
                Les données sont ensuite vérifiées et intégrées dans la base de données avant d'être publiée sur le site
                publique.
            </p>

            <h2>Contributions et remerciements</h2>
            <p>
                Philippe de Deul'air pour le contrôles des calculs.
                <br />
                Mathilde d'Entrelianes pour la catégorisation des populations et la déductions des légendes et couleurs.
                <br />
                Fabien de Lm-Oxygène pour les convertions Lambert 93 vers WGS 84 (4326).
                <br />
                Christophe de Lm-Oxygène pour le développement des applications.
                <br />
                Les bénévoles des associations et les utilisateurs de l'application pour la saisie des données.
            </p>


        </span>

    </div>
</template>

<script>

export default {
    name: "AppInformation",
    components: {
    },
    methods: {
        toPlain(cypher) {
            return cypher.map(code => String.fromCharCode(code)).join('');
        },
        sendRequest() {
            const prefixAsc = [77, 65, 73, 76, 84, 79];
            const recipentAsc = [111, 120, 121, 103, 101, 110, 101, 46, 108, 109];
            const domainAsc = [64, 103, 109, 97, 105, 108, 46, 99, 111, 109];

            const prefix = this.toPlain(prefixAsc);
            const recipent = this.toPlain(recipentAsc)+this.toPlain(domainAsc);
            const subject= "Isochrone: Demande d'accès pour contribution"

            
            window.location.href = `${prefix}:${recipent}?subject=${subject}`;
        }
    }
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
