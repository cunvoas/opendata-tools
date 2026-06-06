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
            <div class="flex gap-2 my-4">
                <button @click="showDiagram = false" :class="['px-4 py-1.5 text-sm rounded transition-colors', showDiagram ? 'bg-slate-100 text-slate-600 hover:bg-slate-200' : 'bg-blue-900 text-white']">Détail</button>
                <button @click="showDiagram = true" :class="['px-4 py-1.5 text-sm rounded transition-colors', showDiagram ? 'bg-blue-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200']">Diagramme</button>
            </div>
            <ol v-if="!showDiagram" class="list-decimal list-inside space-y-2 ml-4">
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
            <div v-else class="flex justify-center my-8">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 560" class="w-full max-w-lg">
                    <defs>
                        <marker id="arrow" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">
                            <path d="M0,0 L10,4 L0,8 Z" fill="#94a3b8" />
                        </marker>
                    </defs>

                    <circle cx="300" cy="30" r="12" fill="#3b82f6" />
                    <text x="300" y="35" text-anchor="middle" fill="#fff" font-size="12" font-weight="bold" font-family="sans-serif">Début</text>

                    <line x1="300" y1="42" x2="300" y2="60" stroke="#94a3b8" stroke-width="2" marker-end="url(#arrow)" />

                    <rect x="70" y="65" width="460" height="55" rx="8" fill="#fff" stroke="#cbd5e1" stroke-width="1.5" />
                    <text x="85" y="88" fill="#1e40af" font-size="14" font-weight="bold" font-family="sans-serif">1. Détermination des carreaux de la commune</text>
                    <text x="85" y="108" fill="#334155" font-size="13" font-family="sans-serif">à partir du cadastre</text>

                    <line x1="300" y1="120" x2="300" y2="145" stroke="#94a3b8" stroke-width="2" marker-end="url(#arrow)" />

                    <rect x="70" y="150" width="460" height="105" rx="8" fill="#fff" stroke="#cbd5e1" stroke-width="1.5" />
                    <text x="85" y="173" fill="#1e40af" font-size="14" font-weight="bold" font-family="sans-serif">2. Pour chaque carreau, calcul de l'accessibilité :</text>
                    <text x="100" y="198" fill="#334155" font-size="13" font-family="sans-serif">• Recherche des isochrones d’accessibilité</text>
                    <text x="100" y="218" fill="#334155" font-size="13" font-family="sans-serif">• Fusion des polygones des isochrones</text>
                    <text x="100" y="238" fill="#334155" font-size="13" font-family="sans-serif">• Détermination des carreaux avec intersection</text>

                    <line x1="300" y1="255" x2="300" y2="280" stroke="#94a3b8" stroke-width="2" marker-end="url(#arrow)" />

                    <rect x="70" y="285" width="460" height="85" rx="8" fill="#fff" stroke="#cbd5e1" stroke-width="1.5" />
                    <text x="85" y="308" fill="#1e40af" font-size="14" font-weight="bold" font-family="sans-serif">3. Calcul de la population couverte :</text>
                    <text x="100" y="333" fill="#334155" font-size="13" font-family="sans-serif">• Complètement recouverts : population totale</text>
                    <text x="100" y="353" fill="#334155" font-size="13" font-family="sans-serif">• Partiellement recouverts : prorata de surface</text>

                    <line x1="300" y1="370" x2="300" y2="395" stroke="#94a3b8" stroke-width="2" marker-end="url(#arrow)" />

                    <rect x="70" y="400" width="460" height="85" rx="8" fill="#fff" stroke="#cbd5e1" stroke-width="1.5" />
                    <text x="85" y="423" fill="#1e40af" font-size="14" font-weight="bold" font-family="sans-serif">4. Calcul de la densité d’espaces verts :</text>
                    <text x="100" y="448" fill="#334155" font-size="13" font-family="sans-serif">• Surface totale des parcs accessibles</text>
                    <text x="100" y="468" fill="#334155" font-size="13" font-family="sans-serif">• m²/habitant calculé</text>

                    <line x1="300" y1="485" x2="300" y2="505" stroke="#94a3b8" stroke-width="2" marker-end="url(#arrow)" />

                    <rect x="260" y="510" width="80" height="30" rx="15" fill="#eff6ff" stroke="#93c5fd" stroke-width="1.5" />
                    <text x="300" y="530" text-anchor="middle" fill="#1e40af" font-size="13" font-weight="bold" font-family="sans-serif">Fin</text>
                </svg>
            </div>
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
                    href="https://www.gnu.org/licenses/agpl-3.0.html" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">licence AGPLv3</a>.
                <br />
                Pour plus d'information, vous pouvez consulter la documentation sur le site du projet.
            </p>
            <div class="mt-4 flex flex-wrap gap-4">
                <a 
                    href="https://github.com/cunvoas/opendata-tools"
                    target="_blank" 
                    rel="noopener noreferrer"
                    class="px-4 py-1.5 text-sm rounded transition-colors bg-blue-900 text-white no-underline">
                    Code Source (AGPLv3)
                </a>
            </div>

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

            <h2 class="font-bold underline my-6">Thésaurus</h2>
            <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                <div class="rounded-lg border border-slate-200 bg-slate-50 p-4 shadow-sm cursor-pointer" @click="showIsochroneDetail = true">
                    <h3 class="text-lg font-semibold">Isochrone</h3>
                    <p class="mt-2 text-sm leading-relaxed">Surface représentant la zone atteignable à pied en un temps donné depuis une entrée de parc. Nous utilisons 5 minutes en zone urbaine et 20 minutes en zone rurale.</p>
                </div>
                <div class="rounded-lg border border-slate-200 bg-slate-50 p-4 shadow-sm cursor-pointer" @click="showM2Detail = true">
                    <h3 class="text-lg font-semibold">m² / habitant</h3>
                    <p class="mt-2 text-sm leading-relaxed">Indicateur de densité d'espaces verts publics. Il correspond au ratio entre la surface totale des parcs accessibles et la population couverte.</p>
                </div>
                <div class="rounded-lg border border-slate-200 bg-slate-50 p-4 shadow-sm cursor-pointer" @click="showCarreauDetail = true">
                    <h3 class="text-lg font-semibold">Carreau INSEE</h3>
                    <p class="mt-2 text-sm leading-relaxed">Maille de référence de 200m × 200m utilisée pour agréger les données socio-démographiques locales et évaluer la population couverte.</p>
                </div>
                <div class="rounded-lg border border-slate-200 bg-slate-50 p-4 shadow-sm cursor-pointer" @click="showEspacesVertsDetail = true">
                    <h3 class="text-lg font-semibold">Espaces verts publics de proximité</h3>
                    <p class="mt-2 text-sm leading-relaxed">Espaces accessibles librement (parcs, jardins, squares) à distance piétonne. Les cimetières ou surfaces minérales ne sont pas comptés dans la surface accessible.</p>
                </div>
                <div class="rounded-lg border border-slate-200 bg-slate-50 p-4 shadow-sm cursor-pointer" @click="showZoneDetail = true">
                    <h3 class="text-lg font-semibold">Zone urbaine / périurbaine</h3>
                    <p class="mt-2 text-sm leading-relaxed">Catégorisation appliquée pour ajuster les seuils OMS : 10 m²/habitant en zone urbaine et 25 m²/habitant en zone périurbaine.</p>
                </div>
            </div>

            <div v-if="showIsochroneDetail" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showIsochroneDetail = false">
                <div class="mx-4 max-w-lg rounded-lg bg-white p-6 shadow-xl">
                    <div class="flex items-start justify-between">
                        <h3 class="text-lg font-semibold">Isochrone</h3>
                        <button @click="showIsochroneDetail = false" class="ml-4 text-slate-400 hover:text-slate-600">&times;</button>
                    </div>
                    <p class="mt-4 text-sm leading-relaxed text-slate-700">
                        Les isochrones sont des indicateurs d'accessibilité spatiale prenant la forme de surfaces correspondant à la zone géographique accessible depuis un point choisi, dans une durée de déplacement prédéfinie. Elles offrent une représentation cartographique relativement simple du temps de parcours nécessaire pour se déplacer vers un lieu ou une ressource particulière. Le principal intérêt des isochrones est de délimiter les espaces disposant d'un accès facile à certaines opportunités.
                    </p>
                    <p class="mt-2 text-sm leading-relaxed text-slate-700">
                        Pour aller plus loin : <a href="https://capamob.cerema.fr/territoire/mesurer-laccessibilite-territoire" target="_blank" rel="noopener noreferrer" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">Mesurer l'accessibilité du territoire - Cerema</a>
                    </p>
                    <button @click="showIsochroneDetail = false" class="mt-4 rounded bg-slate-800 px-4 py-2 text-sm text-white hover:bg-slate-700">Fermer</button>
                </div>
            </div>

            <div v-if="showCarreauDetail" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showCarreauDetail = false">
                <div class="mx-4 max-w-lg rounded-lg bg-white p-6 shadow-xl">
                    <div class="flex items-start justify-between">
                        <h3 class="text-lg font-semibold">Carreau INSEE</h3>
                        <button @click="showCarreauDetail = false" class="ml-4 text-slate-400 hover:text-slate-600">&times;</button>
                    </div>
                    <p class="mt-4 text-sm leading-relaxed text-slate-700">
                        Les carreaux INSEE sont des mailles statistiques de 200m × 200m utilisées par l'Institut national de la statistique et des études économiques pour diffuser des données socio-démographiques à un niveau géographique fin tout en garantissant l'anonymat des personnes. Chaque carreau contient des informations estimées sur la population résidente, les revenus, l'âge ou encore la catégorie socio-professionnelle, sans correspondre à une adresse ou une rue spécifique. Cette approche carroyée permet d'analyser des territoires de manière homogène, indépendamment des limites administratives (communes, iris), et de croiser ces données avec d'autres couches géographiques comme les isochrones d'accès aux parcs.
                    </p>
                    <p class="mt-2 text-sm leading-relaxed text-slate-700">
                        Pour plus d'information : <a :href="inseeCarreauUrl" target="_blank" rel="noopener noreferrer" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">Documentation INSEE sur les carreaux</a>
                    </p>
                    <button @click="showCarreauDetail = false" class="mt-4 rounded bg-slate-800 px-4 py-2 text-sm text-white hover:bg-slate-700">Fermer</button>
                </div>
            </div>

            <div v-if="showEspacesVertsDetail" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showEspacesVertsDetail = false">
                <div class="mx-4 max-w-lg rounded-lg bg-white p-6 shadow-xl">
                    <div class="flex items-start justify-between">
                        <h3 class="text-lg font-semibold">Espaces verts publics de proximité</h3>
                        <button @click="showEspacesVertsDetail = false" class="ml-4 text-slate-400 hover:text-slate-600">&times;</button>
                    </div>
                    <p class="mt-4 text-sm leading-relaxed text-slate-700">
                        L'Organisation mondiale de la santé (OMS / WHO) recommande un minimum de 10 m² d'espaces verts publics de proximité par habitant en zone urbaine, et 25 m²/habitant en zone périurbaine. Les surfaces conseillées sont respectivement de 12 et 45 m²/habitant. Un espace vert public de proximité est un parc, un jardin ou un square accessible librement à distance piétonne (5 minutes en ville, 20 minutes à la campagne). Ces espaces jouent un rôle essentiel pour la santé physique et mentale des citadins : ils favorisent l'activité physique, réduisent le stress, améliorent la qualité de l'air et contribuent à la régulation thermique en ville. Les cimetières, les bosquets non accessibles et les places minéralisées ne sont pas comptabilisés dans cette surface.
                    </p>
                    <button @click="showEspacesVertsDetail = false" class="mt-4 rounded bg-slate-800 px-4 py-2 text-sm text-white hover:bg-slate-700">Fermer</button>
                </div>
            </div>

            <div v-if="showM2Detail" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showM2Detail = false">
                <div class="mx-4 max-w-lg rounded-lg bg-white p-6 shadow-xl">
                    <div class="flex items-start justify-between">
                        <h3 class="text-lg font-semibold">m² / habitant</h3>
                        <button @click="showM2Detail = false" class="ml-4 text-slate-400 hover:text-slate-600">&times;</button>
                    </div>
                    <p class="mt-4 text-sm leading-relaxed text-slate-700">
                        Le ratio <strong>m² / habitant</strong> est l'indicateur clé pour mesurer la desserte en espaces verts publics d'un territoire. Il se calcule en divisant la surface totale des espaces verts publics de proximité accessibles par la population résidente du territoire considéré. Cet indicateur permet de comparer des quartiers, des communes ou des métropoles entre eux, indépendamment de leur taille. L'OMS recommande un minimum de 10 m²/habitant en zone urbaine et 25 m²/habitant en zone périurbaine. Dans cette application, le calcul intègre uniquement les parcs accessibles à pied depuis chaque carreau INSEE, via le réseau de rues, dans le temps de marche préconisé par l'OMS.
                    </p>
                    <button @click="showM2Detail = false" class="mt-4 rounded bg-slate-800 px-4 py-2 text-sm text-white hover:bg-slate-700">Fermer</button>
                </div>
            </div>

            <div v-if="showZoneDetail" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showZoneDetail = false">
                <div class="mx-4 max-w-lg rounded-lg bg-white p-6 shadow-xl">
                    <div class="flex items-start justify-between">
                        <h3 class="text-lg font-semibold">Zone urbaine / périurbaine</h3>
                        <button @click="showZoneDetail = false" class="ml-4 text-slate-400 hover:text-slate-600">&times;</button>
                    </div>
                    <p class="mt-4 text-sm leading-relaxed text-slate-700">
                        D'un point de vue géographique, la <strong>zone urbaine</strong> désigne un espace caractérisé par une forte densité de population et de bâti, une continuité du tissu urbain et une concentration d'activités économiques, de services et d'infrastructures. Elle correspond aux centres-villes et aux quartiers densément peuplés où la trame viaire est continue et les parcs de proximité accessibles en moins de 5 minutes à pied.
                    </p>
                    <p class="mt-2 text-sm leading-relaxed text-slate-700">
                        La <strong>zone périurbaine</strong> (ou couronne périurbaine) est un espace de transition entre la ville et la campagne. Elle se caractérise par une densité de population plus faible, un habitat individuel dominant, une dépendance à la voiture et une distance plus grande aux services et équipements. Les parcs y sont souvent plus vastes mais moins nombreux, et l'accès à pied peut nécessiter jusqu'à 20 minutes. Cette distinction est cruciale pour ajuster les recommandations OMS : les besoins en espaces verts de proximité diffèrent selon la morphologie urbaine et les modes de déplacement dominants.
                    </p>
                    <button @click="showZoneDetail = false" class="mt-4 rounded bg-slate-800 px-4 py-2 text-sm text-white hover:bg-slate-700">Fermer</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

export default {
    name: "AppInformation",
    components: {
    },
    data() {
        return {
            showDiagram: false,
            showIsochroneDetail: false,
            showCarreauDetail: false,
            showEspacesVertsDetail: false,
            showM2Detail: false,
            showZoneDetail: false,
            inseeCarreauUrl: 'https://www.insee.fr/fr/statistiques/8272002'
        }
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
