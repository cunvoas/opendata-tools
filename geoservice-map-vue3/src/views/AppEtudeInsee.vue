<template>
    <div class="text-left align-top">
        <div class="border-2 border-transparent prose max-w-none [&_p]:mb-3">
            <h1 class="text-3xl font-bold mb-4">Espaces verts&nbsp;: l'étude de l'INSEE et la nôtre, pourquoi les chiffres diffèrent</h1>
            <p>
                <strong>Réponse courte&nbsp;:</strong> les deux travaux ne mesurent ni la même population ni le même accès.
                L'INSEE rapporte les espaces verts à la population d'un grand centre urbain avec une zone tampon autour des
                parcs&nbsp;; notre étude rapporte les parcs et jardins de proximité à la population de chaque commune, en
                mesurant l'accès à pied depuis les entrées réelles.
            </p>
            <p>
                En avril 2025, l'INSEE a publié
                <a target="_blank" rel="noopener noreferrer" :href="urlInsee" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">« Un accès inégal aux espaces verts dans les grands centres urbains »</a>
                (Insee Première n°&nbsp;2049). Les deux travaux ne mesurent pas la même chose, avec des méthodes
                différentes.
            </p>
            <div class="overflow-x-auto">
                <table class="w-full text-sm border-collapse">
                    <caption class="sr-only">Comparaison de la méthode de l'INSEE et de celle de l'étude Aut'MEL</caption>
                    <thead>
                        <tr class="bg-slate-100 text-left">
                            <th scope="col" class="p-2 border border-slate-200">Critère</th>
                            <th scope="col" class="p-2 border border-slate-200">INSEE (avril 2025)</th>
                            <th scope="col" class="p-2 border border-slate-200">Étude Aut'MEL</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="ligne in comparaison" :key="ligne.critere">
                            <th scope="row" class="p-2 border border-slate-200 text-left">{{ ligne.critere }}</th>
                            <td class="p-2 border border-slate-200">{{ ligne.insee }}</td>
                            <td class="p-2 border border-slate-200">{{ ligne.autmel }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <h2 class="text-2xl font-bold mt-8 mb-3">Les seuils de m² par habitant</h2>
            <p>
                Les {{ minimumDense }}&nbsp;m² (zone centrale) et {{ minimumPeriurbain }}&nbsp;m²
                (zone périurbaine) par habitant sont les objectifs de la circulaire ministérielle du 8 février 1973, repris
                comme norme par l'OMS selon le Réseau français des Villes-Santé. Le texte de la circulaire précise que le seuil
                de {{ minimumPeriurbain }}&nbsp;m² tient compte des forêts urbaines et des espaces boisés périurbains :
                l'INSEE le rapporte aux forêts urbaines, l'étude l'applique aux parcs et jardins des communes périurbaines.
            </p>
            <h2 class="text-2xl font-bold mt-8 mb-3">La mesure de l'accès</h2>
            <p>
                L'OMS Europe retient comme indicateur la part de la population à moins
                de 300&nbsp;m, en distance linéaire (approximation de 5 minutes de marche), d'un espace vert d'au moins
                0,5&nbsp;ha, et note que la distance par le réseau peut être plus précise mais plus complexe à calculer.
                L'INSEE mesure avec des zones tampons autour des parcs. Notre étude mesure depuis les entrées par le réseau
                de rues, que le Réseau français des Villes-Santé recommande plutôt que la distance à vol d'oiseau.
            </p>
            <h2 class="text-2xl font-bold mt-8 mb-3">Des résultats non directement comparables</h2>
            <p>
                L'INSEE offre une comparaison nationale avec une méthode unique pour toutes les villes. Notre étude part des
                entrées réelles des parcs et du réseau de rues. Les résultats ne sont donc pas directement comparables :
                l'INSEE indique que 51,9&nbsp;% de la population du grand centre urbain de Lille (et non de la commune seule)
                vivait à moins de 300&nbsp;m d'un espace vert en 2019.
            </p>
            <h2 class="text-2xl font-bold mt-8 mb-3">Sources</h2>
            <ul class="list-disc list-inside text-sm space-y-1">
                <li v-for="source in sources" :key="source.url">
                    <a target="_blank" rel="noopener noreferrer" :href="source.url" class="text-blue-900 hover:text-black visited:text-blue-900 underline">{{ source.titre }}</a>
                </li>
            </ul>
            <p class="flex flex-wrap gap-3">
                <router-link to="/#insee" class="px-4 py-2 rounded border border-blue-900 text-blue-900 font-semibold no-underline hover:bg-blue-50">Les chiffres d'espaces verts à Lille et dans la MEL</router-link>
            </p>
        </div>
    </div>
</template>

<script>
import { ANNEE_DONNEES } from '../utils/landingStats.js';

const MINIMUM_DENSE = 10;
const MINIMUM_PERIURBAIN = 25;

export default {
    name: 'AppEtudeInsee',
    data() {
        return {
            minimumDense: MINIMUM_DENSE,
            minimumPeriurbain: MINIMUM_PERIURBAIN,
            urlInsee: 'https://www.insee.fr/fr/statistiques/8558420',
            sources: [
                { titre: 'OMS Europe (2016), Urban green spaces and health', url: 'https://www.who.int/europe/publications/i/item/WHO-EURO-2016-3352-43111-60341' },
                { titre: 'OMS Europe (2017), Urban green spaces: a brief for action', url: 'https://www.who.int/europe/publications/i/item/9789289052498' },
                { titre: 'Réseau français des Villes-Santé de l\'OMS (2020), Espaces verts urbains : promouvoir l\'équité et la santé', url: 'https://villes-sante.com/wp-content/uploads/2023/02/Espaces-verts-urbains.pdf' },
                { titre: 'INSEE (avril 2025), Un accès inégal aux espaces verts dans les grands centres urbains', url: 'https://www.insee.fr/fr/statistiques/8558420' }
            ],
            comparaison: [
                {
                    critere: 'Périmètre',
                    insee: '72 grands centres urbains de France métropolitaine (770 communes)',
                    autmel: 'Communes, intercommunalités et régions de la carte'
                },
                {
                    critere: 'Données sur les parcs',
                    insee: "Extraction d'OpenStreetMap et de la BD TOPO de l'IGN",
                    autmel: 'Données ouvertes des villes et de la MEL, entrées de chaque parc recensées par des bénévoles'
                },
                {
                    critere: "Types d'espaces verts",
                    insee: 'Parcs, jardins et forêts publiques',
                    autmel: 'Parcs et jardins publics de proximité (hors cimetières, bosquets et places minéralisées)'
                },
                {
                    critere: "Mesure de l'accès",
                    insee: 'Zone tampon autour du contour des parcs : 300 m, 600 m ou 900 m selon la taille du parc',
                    autmel: "Isochrone piéton de l'IGN depuis chaque entrée de parc : 300 m en zone dense, 1 200 m ailleurs"
                },
                {
                    critere: 'Population',
                    insee: 'Filosofi 2019, carreaux de 200 m',
                    autmel: 'Filosofi, carreaux de 200 m, millésimes 2015 à 2021'
                },
                {
                    critere: 'Indicateurs',
                    insee: "Part de la population à moins de 5 minutes d'un espace vert ; m² par habitant du territoire",
                    autmel: "Nombre d'habitants par tranche de m² de parc accessible à pied par habitant"
                },
                {
                    critere: 'Seuils de m² par habitant',
                    insee: '10 m² de parcs et jardins ; 25 m² de forêts urbaines',
                    autmel: `${MINIMUM_DENSE} m² en zone dense ; ${MINIMUM_PERIURBAIN} m² en zone périurbaine`
                },
                {
                    critere: 'Source et date',
                    insee: 'Insee Première n° 2049, publié en avril 2025, données 2019',
                    autmel: `Étude Aut'MEL, données Filosofi 2015 à 2021, résultats publiés en ${ANNEE_DONNEES}`
                }
            ]
        };
    }
};
</script>
