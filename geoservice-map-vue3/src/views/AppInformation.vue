<template>
    <div class="text-left align-top">
        <div class="border-2 border-transparent prose max-w-none">
            <h1 class="text-3xl font-bold mb-4">Informations sur l'application</h1>
            <h2 class="font-bold underline my-4">Objectifs</h2>

            <p>
                L'objectif de cette application est de permettre aux habitants des villes et des regroupements de communes de visualiser sur une carte les parcs et jardins qui sont accessibles à pied.
                <br />
                Elle permet aussi de connaître les territoires qui respectent le nombre de m²/habitant d'espaces verts publics de proximité préconisés par l'Organisation mondiale de la santé (OMS).
            </p>

            <h2 class="font-bold underline my-4">Parcs et Jardins</h2>
            <p>
               Les parcs et jardins publics sont des espaces verts accessibles qui permettent de se détendre, de se promener, de jouer, de se reposer, de pique-niquer, de lire, de se rencontrer, de s’aérer...
               <br />
                Certaines villes comptabilisent les cimetières, les bosquets, des places minéralisées comme des parcs et jardins. Ici, l'application les exclut des surfaces pour être en cohérence avec les critères retenus par l'OMS, mais les noms sont disponibles dans les détails avec une croix.
            </p>
            <p>
                Ils sont intégrés et géolocalisés sur une carte interactive qui permet d'évaluer les populations qui
                peuvent en bénéficier.
                <br />
                Nous avons intégré les recommandations de l'OMS qui préconisent au minimum 10m²/habitant d'espaces verts publics de proximité en zone urbaine et 25 m²/habitant en zone périurbaine (les surfaces conseillées sont respectivement de 12 et 45 m²/habitant). De plus, la distance pour accéder aux parcs est de 5 minutes à pied (soit 300m) en ville et 20 minutes à la campagne (soit 1200m).
                <br />


                <br />
                La zone d'accessibilité isochrone est établie par l'intermédiaire du service de calcul d'isochrone de
                l'IGN. Les données de densité des communes, des populations, des données carroyées sont fournies par
                l'INSEE. Le cadastre provient du site opendata du gouvernement.
                <br />
                Certaines villes proposent également les données de parc via opendata et elles ont été intégrées.
                <br />
            </p>
            <h2 class="font-bold underline my-4">Les données utilisées et l'algorithme</h2>
            <p>
                Nous utilisons les données disponibles en OpenData lorsque cela est possible auxquelles s'ajoute un travail
                collaboratif de plusieurs associations qui a permis de recenser les entrées de chaque parc.<br />
                Pour chaque entrée, nous déterminons la zone d'accessibilité "isochrone" grâce au service de calcul
                d'isochrone de l'IGN.<br />
                Les données de densité des communes, des populations, des données carroyées sont fournies par l'INSEE.
            </p>
            <p class="font-semibold">
                L'algorithme de calcul de la zone d'accessibilité est le suivant :
            </p>
            <ol class="list-decimal list-inside space-y-2 ml-4">
                <li class="font-medium">Détermination des carreaux de la commune à partir du cadastre</li>
                
                <li class="font-medium">Pour chaque carreau, calcul de l'accessibilité :
                    <ul class="list-disc list-inside ml-6 mt-2 space-y-1">
                        <li>Recherche des isochrones d'accessibilité des parcs ayant une intersection avec le carreau</li>
                        <li>Fusion des polygones des isochrones identifiés</li>
                        <li>Détermination de tous les carreaux ayant une intersection avec ce polygone d'accès</li>
                    </ul>
                </li>
                
                <li class="font-medium">Calcul de la population couverte :
                    <ul class="list-disc list-inside ml-6 mt-2 space-y-1">
                        <li>Pour les carreaux complètement recouverts par le polygone : toute la population est comptabilisée</li>
                        <li>Pour les carreaux partiellement recouverts : la population est estimée au prorata de la surface de recouvrement</li>
                    </ul>
                </li>
                
                <li class="font-medium">Calcul de la densité d'espaces verts :
                    <ul class="list-disc list-inside ml-6 mt-2 space-y-1">
                        <li>La surface totale de tous les parcs accessibles est comptabilisée</li>
                        <li>La densité (m²/habitant) est calculée en fonction de la population ayant accès aux parcs</li>
                    </ul>
                </li>
            </ol>
            <br />
            <p>
                Ce calcul est réalisé avec les dernières données de l'INSEE rendues publiques.
            </p>

            <h2 class="font-bold underline my-4">Code source et contribution à l'application</h2>
            <p>
                Le programme est disponible en opensource. Il a été réalisé par des bénévoles et des associations. Il
                est
                possible de contribuer à l'amélioration des données, des algorithmes et des fonctionnalités.
                <br />
                Pour cela, il suffit de se rendre sur le <a target="_blank" rel="noopener noreferrer"
                    href="https://github.com/cunvoas/opendata-tools/issues">site github du projet</a> et de proposer des
                améliorations.
                <br />
                Le programme peut aussi être copié en respectant la <a target="_blank" rel="noopener noreferrer"
                    href="https://www.gnu.org/licenses/gpl-3.0.fr.html" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">licence GPLv3</a>.
                <br />
                Pour plus d'information, vous pouvez consulter la documentation sur le site du projet.
            </p>

            <h2 class="font-bold underline my-4">Contribution à la saisie</h2>
            <p>
                Les contributions aux données sont réalisées sur une application dédiée. Pour y accéder, il faut en
                faire la
                demande via <a href="javascript:void(0);" @click="sendRequest" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold cursor-pointer">email</a>
                <br />
                Nous limitons le nombre de contributeurs par territoire afin de garantir la qualité des données et la
                stabilité de notre application.
                <br />
                Les données sont ensuite vérifiées et intégrées dans la base de données avant d'être publiées sur le site
                public.
            </p>

            <h2 class="font-bold underline my-4">Contributions et remerciements</h2>
            <p>
                Philippe de Deûl'Air pour le contrôle des calculs.
                <br />
                Mathilde d'Entrelianes pour la catégorisation des populations et la déduction des légendes et couleurs.
                <br />
                Fabien de LM-Oxygène pour les conversions Lambert 93 vers WGS 84 (4326).
                <br />
                Christophe de LM-Oxygène pour le développement des applications.
                <br />
                Les bénévoles des associations et les utilisateurs de l'application pour la saisie des données.
            </p>
        </div>
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
            const recipientAsc = [111, 120, 121, 103, 101, 110, 101, 46, 108, 109];
            const domainAsc = [64, 103, 109, 97, 105, 108, 46, 99, 111, 109];

            const prefix = this.toPlain(prefixAsc);
            const recipient = this.toPlain(recipientAsc)+this.toPlain(domainAsc);
            const subject= "Isochrone: Demande d'accès pour contribution"

            
            window.location.href = `${prefix}:${recipient}?subject=${subject}`;
        }
    }
};
</script>
