<template>
    <div class="text-left align-top" :data-landing-ready="pret ? 'true' : 'false'">
        <div class="border-2 border-transparent prose max-w-none">
            <h1 class="text-3xl font-bold mb-4">Combien de m² de parcs et jardins par habitant dans la Métropole Européenne de Lille&nbsp;?</h1>
            <p class="text-lg">
                Cette étude porte sur les espaces verts publics de proximité de Lille et de la Métropole Européenne de
                Lille (MEL). Elle mesure, pour chaque habitant, la surface de parcs et jardins publics accessibles à pied
                depuis leurs entrées réelles, et la compare aux seuils de référence repris par l'OMS. Elle est réalisée par
                des associations à partir de données ouvertes. Carte, statistiques, méthode et code source sont consultables
                par tous.
            </p>
            <p class="flex flex-wrap gap-3">
                <router-link to="/carte" :class="boutonPrincipal">Voir la carte</router-link>
                <router-link to="/stats" :class="boutonSecondaire">Statistiques par commune</router-link>
            </p>

            <section aria-labelledby="chiffres-cles">
                <h2 id="chiffres-cles" class="text-2xl font-bold mt-8 mb-3">Espaces verts à Lille et dans la MEL : les chiffres clés</h2>
                <div v-if="cartes.length" class="grid gap-4 md:grid-cols-2">
                    <div v-for="carte in cartes" :key="carte.cle" class="rounded-lg border border-slate-200 bg-slate-50 p-4 shadow-sm">
                        <h3 class="text-lg font-semibold m-0">{{ carte.titre }}</h3>
                        <p class="text-4xl font-bold text-blue-900 my-2">{{ carte.pourcent }}&nbsp;%</p>
                        <p class="m-0">{{ carte.texte }}</p>
                        <p v-if="carte.detail" class="mt-2 mb-0 text-sm">{{ carte.detail }}</p>
                        <p v-if="carte.moyenne != null" class="mt-2 mb-0 text-sm">
                            En moyenne, un habitant dispose de <strong>{{ carte.moyenne }}&nbsp;m²</strong> de parc accessible à pied
                            (moyenne des m² par habitant de chaque carreau, pondérée par la population).
                        </p>
                        <p v-if="carte.global != null" class="mt-2 mb-0 text-sm">
                            Toutes les surfaces de parcs comptées une seule fois, rapportées à la population prise en compte&nbsp;:
                            <strong>{{ carte.global }}&nbsp;m² par habitant</strong>.
                        </p>
                    </div>
                </div>
                <p v-else-if="chargement" role="status">Chargement des chiffres…</p>
                <p v-else>Les chiffres ne sont pas disponibles pour le moment. Ils restent consultables dans les statistiques.</p>
                <p v-if="cartes.length" class="text-sm">
                    Données {{ annee }}. Habitants pris en compte :
                    <template v-if="lille">Lille {{ nombre(lille.habitants) }}</template><template v-if="lille && mel"> ; </template><template v-if="mel">MEL {{ nombre(mel.habitants) }}</template>
                    (population des ménages fiscaux, INSEE Filosofi, carreaux de 200&nbsp;m). Les habitants qui n'ont aucun parc
                    accessible à pied sont comptés dans la première tranche.
                    <router-link to="/information" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">Méthode détaillée</router-link>
                </p>
            </section>

            <section aria-labelledby="chez-vous" class="[&_p]:mb-3">
                <h2 id="chez-vous" class="text-2xl font-bold mt-8 mb-3">Combien de m² de parc autour de chez vous&nbsp;?</h2>
                <p>
                    Ces chiffres sont des moyennes. Sur la carte, saisissez votre adresse dans «&nbsp;Aller à une adresse&nbsp;» :
                    en survolant le carreau de 200&nbsp;m sur 200&nbsp;m qui l'entoure, vous lisez la surface de parc accessible à
                    pied (en m²), le nombre d'habitants avec lesquels elle est partagée, le résultat en m² par habitant et la
                    liste des parcs concernés.
                </p>
                <figure class="m-0 mb-3">
                    <img :src="imageCarte" width="1200" height="630" loading="lazy" decoding="async"
                         alt="Carte de Lille : chaque carreau de 200 m est coloré du bleu au vert selon les m² de parc accessibles à pied par habitant"
                         class="w-full h-auto rounded border border-slate-200" />
                    <figcaption class="text-sm text-slate-600 mt-1">Capture de la carte de Lille : plus le carreau est vert, plus la surface de parc accessible à pied par habitant est élevée. Fond de carte © OpenStreetMap.</figcaption>
                </figure>
                <p class="flex flex-wrap gap-3">
                    <router-link to="/carte" :class="boutonPrincipal">Saisir mon adresse sur la carte</router-link>
                </p>
            </section>

            <section aria-labelledby="ce-que-mesure" class="[&_p]:mb-3 [&_ul]:mb-3">
                <h2 id="ce-que-mesure" class="text-2xl font-bold mt-8 mb-3">Comment nous mesurons l'accès aux parcs à pied</h2>
                <ul class="list-disc list-inside space-y-2">
                    <li>
                        <strong>L'accès réel à pied.</strong> La zone atteignable en 5 minutes (300&nbsp;m) en zone dense et en
                        20 minutes (1&nbsp;200&nbsp;m) ailleurs est calculée depuis chaque entrée de parc, en suivant le réseau de
                        rues (isochrone de l'IGN).
                    </li>
                    <li>
                        <strong>Les parcs de proximité.</strong> Parcs et jardins publics où l'on se détend, se promène et joue.
                        Les cimetières, bosquets et places minéralisées ne comptent pas dans les surfaces.
                    </li>
                    <li>
                        <strong>La population réelle.</strong> Les habitants sont comptés par carreaux de 200&nbsp;m sur 200&nbsp;m de
                        l'INSEE, au prorata de la surface couverte par la zone d'accès.
                    </li>
                    <li>
                        <strong>Les seuils retenus.</strong> {{ minimumDense }}&nbsp;m² par habitant en zone dense,
                        {{ minimumPeriurbain }}&nbsp;m² en zone périurbaine. Leur origine est précisée plus bas.
                    </li>
                </ul>
            </section>

            <section aria-labelledby="genese" class="[&_p]:mb-3 [&_ul]:mb-3">
                <h2 id="genese" class="text-2xl font-bold mt-8 mb-3">La genèse de l'outil</h2>
                <p>
                    <strong>L'outil est né d'un chiffre qui ne collait pas.</strong> LM Oxygène et Deûl'Air, qui œuvrent toutes
                    deux pour la nature en ville, ont chacune calculé à la main les surfaces de parcs de leur quartier. Le compte
                    n'y était pas avec les 17&nbsp;m² par habitant annoncés par la ville.
                </p>
                <p>
                    Après des échanges sur la méthode la plus précise et la plus factuelle, nous avons créé une mini-application.
                    Elle s'est enrichie au fil des années, en fonctionnalités comme en étendue de données.
                </p>
                <p>
                    Les premiers résultats recoupaient nos calculs faits à la main. Nous avons alors élargi le périmètre : Lille,
                    Hellemmes et Lomme, puis toutes les communes de la MEL.
                </p>
                <p>
                    Ce travail a été présenté le <time datetime="2026-01-15">15 janvier 2026</time> lors de la conférence-débat
                    «&nbsp;De la friche Saint-Sauveur aux Rives de la Haute-Deûle : quelles alternatives à la densification
                    urbaine&nbsp;?&nbsp;», à Sciences Po Lille, avec l'Arène et la Ruche. Elle a marqué le démarrage des débats
                    publics entre les candidats aux municipales de 2026 à Lille.
                    <a target="_blank" rel="noopener noreferrer" :href="urlReportage" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">Médiacités</a>
                    et
                    <a target="_blank" rel="noopener noreferrer" :href="urlDepeche" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">AEF info</a>
                    en ont rendu compte.
                </p>

                <p>
                    Depuis le printemps 2026, l'outil va plus loin : il sait aussi simuler l'ajout de parcs et en mesurer l'effet
                    pour les habitants, en comparant la situation avant et après. Cette fonction n'est disponible que dans notre
                    système de gestion.
                </p>
                <p>
                    Encore faut-il savoir où poser ces parcs. C'est un casse-tête : le nombre de combinaisons possibles est
                    immense. L'outil s'appuie pour cela sur la recherche opérationnelle, la branche des mathématiques qui cherche
                    la meilleure solution parmi un très grand nombre de possibilités, comme pour un emploi du temps ou une
                    tournée de livraison. Parmi les méthodes disponibles&nbsp;:
                </p>
                <ul class="list-disc list-inside space-y-2">
                    <li v-for="methode in methodes" :key="methode.cle">
                        <strong>{{ methode.titre }}</strong> ({{ methode.technique }})&nbsp;: {{ methode.texte }}
                    </li>
                </ul>
                <p>
                    Autre nouveauté, ajoutée pendant l'été 2026&nbsp;: un mode manuel. Plutôt que de laisser l'ordinateur proposer
                    des emplacements, on dessine soi-même un futur parc sur la carte, d'un simple cercle ou d'un contour précis.
                </p>
                <p>
                    Un clic sur «&nbsp;Évaluer&nbsp;», et la carte se colore, carreau de 200&nbsp;m par carreau. Chacun affiche, avec
                    sa population, les m² de parc par habitant et le manque de surface qui subsiste, avant et après l'ajout.
                </p>
                <p>
                    <strong>Une précision importante&nbsp;:</strong> cette évaluation est simplifiée. Un parc dessiné compte pour un
                    carreau dès qu'il se trouve, à vol d'oiseau, à moins de la distance de marche de référence (avec une petite
                    marge), sans calcul d'itinéraire depuis des entrées comme dans l'étude principale. L'outil est en version bêta,
                    comme la simulation automatique, et lui aussi réservé à notre système de gestion.
                </p>
            </section>

            <section aria-labelledby="projet-en-chiffres">
                <h2 id="projet-en-chiffres" class="text-2xl font-bold mt-8 mb-3">Parcs recensés et entrées saisies : le projet en chiffres</h2>
                <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    <div v-for="chiffre in chiffresProjet" :key="chiffre.cle" class="rounded-lg border border-slate-200 bg-slate-50 p-4 shadow-sm">
                        <p class="text-3xl font-bold text-blue-900 m-0">{{ chiffre.valeur }}</p>
                        <p class="m-0 mt-1 text-sm">{{ chiffre.libelle }}</p>
                    </div>
                </div>
                <p class="text-sm">
                    Au {{ dateChiffres }}. L'application propose {{ nombre(chiffres.intercommunalites) }} intercommunalités et
                    {{ nombre(chiffres.communes) }} communes ; les résultats n'existent que là où des associations ont décrit les parcs.
                </p>
            </section>

            <section aria-labelledby="insee" class="[&_p]:mb-3 [&_ul]:mb-3">
                <h2 id="insee" class="text-2xl font-bold mt-8 mb-3">Espaces verts : l'étude de l'INSEE et la nôtre, pourquoi les chiffres diffèrent</h2>
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
                <p>
                    <strong>Les seuils.</strong> Les {{ minimumDense }}&nbsp;m² (zone centrale) et {{ minimumPeriurbain }}&nbsp;m²
                    (zone périurbaine) par habitant sont les objectifs de la circulaire ministérielle du 8 février 1973, repris
                    comme norme par l'OMS selon le Réseau français des Villes-Santé. Le texte de la circulaire précise que le seuil
                    de {{ minimumPeriurbain }}&nbsp;m² tient compte des forêts urbaines et des espaces boisés périurbains :
                    l'INSEE le rapporte aux forêts urbaines, l'étude l'applique aux parcs et jardins des communes périurbaines.
                </p>
                <p>
                    <strong>La mesure de l'accès.</strong> L'OMS Europe retient comme indicateur la part de la population à moins
                    de 300&nbsp;m, en distance linéaire (approximation de 5 minutes de marche), d'un espace vert d'au moins
                    0,5&nbsp;ha, et note que la distance par le réseau peut être plus précise mais plus complexe à calculer.
                    L'INSEE mesure avec des zones tampons autour des parcs. Notre étude mesure depuis les entrées par le réseau
                    de rues, que le Réseau français des Villes-Santé recommande plutôt que la distance à vol d'oiseau.
                </p>
                <p>
                    L'INSEE offre une comparaison nationale avec une méthode unique pour toutes les villes. Notre étude part des
                    entrées réelles des parcs et du réseau de rues. Les résultats ne sont donc pas directement comparables :
                    par exemple, les 51,9&nbsp;% de la population de Lille à moins de 300&nbsp;m d'un espace vert publiés par
                    l'INSEE (données 2019) concernent le grand centre urbain de Lille, et non la seule commune.
                </p>
                <p class="text-sm font-semibold mb-1">Sources</p>
                <ul class="list-disc list-inside text-sm space-y-1">
                    <li v-for="source in sources" :key="source.url">
                        <a target="_blank" rel="noopener noreferrer" :href="source.url" class="text-blue-900 hover:text-black visited:text-blue-900 underline">{{ source.titre }}</a>
                    </li>
                </ul>
            </section>

            <section aria-labelledby="demarche" class="[&_p]:mb-3 [&_ul]:mb-3">
                <h2 id="demarche" class="text-2xl font-bold mt-8 mb-3">Une démarche citoyenne et ouverte</h2>
                <p>
                    L'étude est portée par Aut'MEL, Deûl'Air, Entrelianes et LM Oxygène. Des bénévoles recensent les entrées
                    de chaque parc, et les données sont vérifiées avant publication. Le code source est publié sous
                    <a target="_blank" rel="noopener noreferrer" href="https://www.gnu.org/licenses/agpl-3.0.html" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">licence AGPLv3</a>,
                    et les données publiées sont publiques.
                </p>
                <p class="flex flex-wrap gap-3">
                    <a target="_blank" rel="noopener noreferrer" href="https://github.com/cunvoas/opendata-tools" :class="boutonSecondaire">Code source</a>
                    <a target="_blank" rel="noopener noreferrer" href="https://github.com/autmel/geoservice-data" :class="boutonSecondaire">Données publiées</a>
                    <router-link to="/information" :class="boutonSecondaire">Méthode détaillée</router-link>
                </p>
            </section>

            <section aria-labelledby="votre-territoire" class="[&_p]:mb-3 [&_ul]:mb-3">
                <h2 id="votre-territoire" class="text-2xl font-bold mt-8 mb-3">Et votre territoire&nbsp;? Étendre l'étude à votre intercommunalité</h2>
                <p>
                    Nous pouvons réaliser ce travail pour toutes les intercommunalités de France, à condition que des associations
                    de chaque territoire décrivent leurs parcs. Environ {{ nombre(chiffres.parcsAFranceMetropolitaine) }} parcs sont
                    à traiter en France métropolitaine, d'après les données OpenStreetMap.
                </p>
                <p class="flex flex-wrap gap-3">
                    <button type="button" :class="boutonPrincipal" @click="contribuer">Décrire les parcs de mon territoire</button>
                    <router-link to="/carte" :class="boutonSecondaire">Voir ma commune sur la carte</router-link>
                </p>
            </section>
        </div>
    </div>
</template>

<script>
import {
    chargerResume, urlStatsCommune, urlStatsEpci, lireDonneesIntegrees, integrerDonnees, COMMUNE_LILLE, EPCI_MEL
} from '../utils/landingStats.js';
import { SITE_URL } from '../utils/seoMeta.js';
import { ecrireAuProjet } from '../utils/contact.js';
import { CHIFFRES_DU, CHIFFRES_PROJET } from '../utils/chiffresProjet.js';

const MINIMUM_DENSE = 10;
const MINIMUM_PERIURBAIN = 25;
const ID_JSON_LD = 'json-ld-accueil';
const nombre = (valeur) => new Intl.NumberFormat('fr-FR').format(valeur);
const pourcent = (valeur) => new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 1 }).format(valeur);
const decimal = (valeur) => (valeur == null ? null : pourcent(valeur));

export default {
    name: 'AppLanding',
    data() {
        const c = CHIFFRES_PROJET;
        // Chiffres déjà présents dans le HTML pré-rendu : affichés dès le premier rendu, sans « Chargement… »
        const integrees = lireDonneesIntegrees();
        return {
            lille: integrees.lille ?? null,
            mel: integrees.mel ?? null,
            chargement: !(integrees.lille && integrees.mel),
            imageCarte: `${import.meta.env.BASE_URL}og-carte-lille-1200x630.jpg`,
            minimumDense: MINIMUM_DENSE,
            minimumPeriurbain: MINIMUM_PERIURBAIN,
            urlInsee: 'https://www.insee.fr/fr/statistiques/8558420',
            urlReportage: 'https://www.mediacites.fr/reportage/lille/2026/01/16/a-lille-les-candidats-sopposent-a-nouveau-sur-lamenagement-de-la-friche-saint-sauveur/',
            urlDepeche: 'https://www.aefinfo.fr/depeche/744403-a-lille-lopposition-remet-en-question-le-modele-de-densification-urbaine',
            boutonPrincipal: 'px-4 py-2 rounded bg-blue-900 text-white font-semibold no-underline hover:bg-blue-800 cursor-pointer',
            boutonSecondaire: 'px-4 py-2 rounded border border-blue-900 text-blue-900 font-semibold no-underline hover:bg-blue-50',
            methodes: [
                {
                    cle: 'contraintes',
                    titre: "Les règles d'abord",
                    technique: 'programmation par contraintes',
                    texte: "on fixe les règles du jeu, comme la surface minimale d'un parc, et le logiciel élimine toutes les combinaisons qui les enfreignent, jusqu'à trouver une solution exacte."
                },
                {
                    cle: 'genetique',
                    titre: "L'évolution",
                    technique: 'algorithme génétique',
                    texte: "comme dans la nature, les meilleures propositions survivent. L'ordinateur en fabrique un grand nombre, garde les plus convaincantes, les croise et les modifie légèrement, génération après génération. Il obtient de très bonnes solutions quand le problème est trop vaste pour un calcul exact, sans garantie que ce soit la meilleure."
                },
                {
                    cle: 'glouton',
                    titre: 'Pas à pas',
                    technique: 'algorithme glouton',
                    texte: "on traite les secteurs de 200 m sur 200 m un par un, en commençant par celui où le manque de parc est le plus fort, puis on mesure l'effet sur ses voisins. C'est très rapide, mais pas toujours optimal : à chaque étape, on prend ce qui semble le mieux sur le moment."
                }
            ],
            chiffres: c,
            chiffresProjet: [
                { cle: 'parcs', valeur: nombre(c.parcsReferences), libelle: `parcs référencés, dont ${nombre(c.parcsVerifies)} vérifiés` },
                { cle: 'entrees', valeur: nombre(c.entreesDeParc), libelle: 'entrées de parc saisies' },
                { cle: 'associations', valeur: nombre(c.associations), libelle: 'associations' },
                { cle: 'carreaux', valeur: nombre(c.carreaux), libelle: `carreaux de 200 m, sur ${c.millesimes} millésimes de données carroyées (2015, 2017, 2019, 2021)` }
            ],
            dateChiffres: new Intl.DateTimeFormat('fr-FR', { dateStyle: 'long', timeZone: 'UTC' }).format(new Date(CHIFFRES_DU)),
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
                }
            ]
        };
    },
    computed: {
        pret() {
            return Boolean(this.lille && this.mel);
        },
        annee() {
            return this.lille?.annee || this.mel?.annee || '';
        },
        cartes() {
            const cartes = [];
            if (this.lille) {
                const minimum = this.lille.minimumM2;
                const detail = this.lille.premiereTranche != null && this.lille.premiereBorne != null
                    ? `${pourcent(this.lille.premiereTranche)} % ont moins de ${this.lille.premiereBorne} m², y compris ceux qui n'ont aucun parc accessible à pied.`
                    : '';
                cartes.push({
                    cle: 'lille',
                    titre: 'Lille',
                    pourcent: pourcent(this.lille.sousMinimum),
                    texte: minimum
                        ? `des habitants disposent de moins de ${minimum} m² de parc accessible à pied, le seuil retenu pour la commune.`
                        : 'des habitants vivent sous le seuil retenu pour la commune.',
                    detail,
                    moyenne: decimal(this.lille.m2Moyen),
                    global: decimal(this.lille.m2Global)
                });
            }
            if (this.mel) {
                cartes.push({
                    cle: 'mel',
                    titre: 'Métropole Européenne de Lille',
                    pourcent: pourcent(this.mel.sousMinimum),
                    texte: `des habitants vivent sous le seuil de leur commune : ${MINIMUM_DENSE} m² par habitant en zone dense, ${MINIMUM_PERIURBAIN} m² en zone périurbaine.`,
                    detail: '',
                    moyenne: decimal(this.mel.m2Moyen),
                    global: decimal(this.mel.m2Global)
                });
            }
            return cartes;
        }
    },
    async mounted() {
        this.injecterJsonLd();
        const [lille, mel] = await Promise.all([
            chargerResume(urlStatsCommune(COMMUNE_LILLE)),
            chargerResume(urlStatsEpci(EPCI_MEL))
        ]);
        // Un échec réseau garde les chiffres intégrés au HTML plutôt que d'effacer la page
        this.lille = lille ?? this.lille;
        this.mel = mel ?? this.mel;
        this.chargement = false;
        if (this.lille && this.mel) integrerDonnees({ lille: this.lille, mel: this.mel });
    },
    beforeUnmount() {
        document.getElementById(ID_JSON_LD)?.remove();
    },
    methods: {
        nombre,
        contribuer() {
            ecrireAuProjet("Isochrone: Décrire les parcs de mon territoire");
        },
        injecterJsonLd() {
            const script = document.createElement('script');
            script.type = 'application/ld+json';
            script.id = ID_JSON_LD;
            const auteurs = ["Aut'MEL", "Deûl'Air", 'Entrelianes', 'LM Oxygène'].map((name) => ({ '@type': 'Organization', name }));
            script.textContent = JSON.stringify({
                '@context': 'https://schema.org',
                '@graph': [
                    {
                        '@type': 'WebPage',
                        '@id': `${SITE_URL}/#accueil`,
                        url: `${SITE_URL}/`,
                        name: this.$route?.meta?.title || document.title,
                        description: this.$route?.meta?.description || '',
                        inLanguage: 'fr-FR',
                        about: "Accès aux parcs et jardins publics de la Métropole Européenne de Lille selon les seuils de référence repris par l'OMS",
                        author: auteurs
                    },
                    {
                        // Pas de « license » : la licence des données publiées n'est pas arrêtée (seul le code est sous AGPLv3)
                        '@type': 'Dataset',
                        '@id': `${SITE_URL}/#donnees`,
                        name: "Parcs et jardins accessibles à pied par habitant, Métropole Européenne de Lille",
                        description: "Nombre d'habitants par tranche de m² de parc public accessible à pied par habitant, par commune et par intercommunalité, calculé avec des isochrones piétons depuis les entrées des parcs et la population des carreaux de 200 m de l'INSEE.",
                        url: 'https://github.com/autmel/geoservice-data',
                        inLanguage: 'fr-FR',
                        isAccessibleForFree: true,
                        creator: auteurs,
                        temporalCoverage: '2015/2021',
                        spatialCoverage: { '@type': 'Place', name: 'Métropole Européenne de Lille' },
                        variableMeasured: 'Surface de parc public accessible à pied par habitant (m² par habitant)',
                        measurementTechnique: "Isochrones piétons de l'IGN depuis les entrées de parc ; population des carreaux de 200 m (INSEE, Filosofi)",
                        distribution: [{ '@type': 'DataDownload', encodingFormat: 'application/json', contentUrl: urlStatsEpci(EPCI_MEL) }]
                    }
                ]
            });
            document.head.appendChild(script);
        }
    }
};
</script>
