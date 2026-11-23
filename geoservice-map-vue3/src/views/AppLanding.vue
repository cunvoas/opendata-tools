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
                        {{ minimumPeriurbain }}&nbsp;m² en zone périurbaine. Leur origine est précisée dans
                        <router-link to="/etude-insee" class="text-blue-900 hover:text-black visited:text-blue-900 underline">la comparaison avec l'étude de l'INSEE</router-link>.
                    </li>
                </ul>
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
            <section aria-labelledby="chez-vous" class="[&_p]:mb-3">
                <h2 id="chez-vous" class="text-2xl font-bold mt-8 mb-3">Combien de m² de parc autour de chez vous&nbsp;?</h2>
                <div class="grid gap-6 md:grid-cols-2 md:items-start">
                    <div>
                        <p>
                            Ces chiffres sont des moyennes. Sur la carte, saisissez votre adresse dans «&nbsp;Aller à une adresse&nbsp;» :
                            en survolant le carreau de 200&nbsp;m sur 200&nbsp;m qui l'entoure, vous lisez la surface de parc accessible à
                            pied (en m²), le nombre d'habitants avec lesquels elle est partagée, le résultat en m² par habitant et la
                            liste des parcs concernés.
                        </p>
                        <p class="flex flex-wrap gap-3">
                            <router-link to="/carte" :class="boutonPrincipal">Saisir mon adresse sur la carte</router-link>
                        </p>
                    </div>
                    <figure class="m-0">
                        <img :src="imageCarte" width="1200" height="630" loading="lazy" decoding="async"
                             alt="Carte de Lille : chaque carreau de 200 m est coloré du bleu au vert selon les m² de parc accessibles à pied par habitant"
                             class="w-full h-auto rounded border border-slate-200" />
                        <figcaption class="text-sm text-slate-600 mt-1">Capture de la carte de Lille : plus le carreau est vert, plus la surface de parc accessible à pied par habitant est élevée. Fond de carte © OpenStreetMap.</figcaption>
                    </figure>
                </div>
            </section>
            <section aria-labelledby="communes" class="[&_p]:mb-3">
                <h2 id="communes" class="text-2xl font-bold mt-8 mb-3">Les espaces verts commune par commune</h2>
                <p>
                    Chaque commune couverte dispose de sa page : répartition des habitants par surface de parc accessible à pied
                    et comparaison aux seuils de l'OMS.
                </p>
                <ul class="list-disc list-inside columns-1 sm:columns-2 lg:columns-3">
                    <li v-for="commune in liensCommunes" :key="commune.insee">
                        <router-link :to="urlCommune(commune.insee)" class="text-blue-900 hover:text-black visited:text-blue-900 underline">{{ commune.nom }}</router-link>
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
            <section aria-labelledby="insee" class="[&_p]:mb-3">
                <h2 id="insee" class="text-2xl font-bold mt-8 mb-3">Espaces verts : l'étude de l'INSEE et la nôtre</h2>
                <p>
                    <strong>Réponse courte&nbsp;:</strong> les deux travaux ne mesurent ni la même population ni le même accès.
                    L'INSEE rapporte les espaces verts à la population d'un grand centre urbain avec une zone tampon autour des
                    parcs&nbsp;; notre étude rapporte les parcs et jardins de proximité à la population de chaque commune, en
                    mesurant l'accès à pied depuis les entrées réelles. L'INSEE indique ainsi que 51,9&nbsp;% de la population du
                    grand centre urbain de Lille (et non de la commune seule) vivait à moins de 300&nbsp;m d'un espace vert en 2019.
                </p>
                <p>
                    <router-link to="/etude-insee" class="text-blue-900 hover:text-black visited:text-blue-900 underline font-semibold">L'étude de l'INSEE et la nôtre, pourquoi les chiffres diffèrent&nbsp;: périmètre, seuils, mesure de l'accès, tableau comparatif et sources</router-link>
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
    chargerResume, urlsStatsCommune, urlsStatsEpciType, urlStatsEpci, dossierEpciMel,
    lireDonneesIntegrees, integrerDonnees, COMMUNE_LILLE
} from '../utils/landingStats.js';
import { SITE_URL } from '../utils/seoMeta.js';
import { communesPourLiens, urlCommune } from '../utils/territoires.js';
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
            urlReportage: 'https://www.mediacites.fr/reportage/lille/2026/01/16/a-lille-les-candidats-sopposent-a-nouveau-sur-lamenagement-de-la-friche-saint-sauveur/',
            urlDepeche: 'https://www.aefinfo.fr/depeche/744403-a-lille-lopposition-remet-en-question-le-modele-de-densification-urbaine',
            boutonPrincipal: 'px-4 py-2 rounded bg-blue-900 text-white font-semibold no-underline hover:bg-blue-800 cursor-pointer',
            boutonSecondaire: 'px-4 py-2 rounded border border-blue-900 text-blue-900 font-semibold no-underline hover:bg-blue-50',
            chiffres: c,
            liensCommunes: communesPourLiens(50),
            urlCommune,
            chiffresProjet: [
                { cle: 'parcs', valeur: nombre(c.parcsReferences), libelle: `parcs référencés, dont ${nombre(c.parcsVerifies)} vérifiés` },
                { cle: 'entrees', valeur: nombre(c.entreesDeParc), libelle: 'entrées de parc saisies' },
                { cle: 'associations', valeur: nombre(c.associations), libelle: 'associations' },
                { cle: 'carreaux', valeur: nombre(c.carreaux), libelle: `carreaux de 200 m, sur ${c.millesimes} millésimes de données carroyées (2015, 2017, 2019, 2021)` }
            ],
            dateChiffres: new Intl.DateTimeFormat('fr-FR', { dateStyle: 'long', timeZone: 'UTC' }).format(new Date(CHIFFRES_DU))
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
        const dossierMel = dossierEpciMel();
        const [lille, mel] = await Promise.all([
            chargerResume(urlsStatsCommune(COMMUNE_LILLE)),
            dossierMel ? chargerResume(urlsStatsEpciType(dossierMel)) : null
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
            const dossierMel = dossierEpciMel();
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
                        distribution: dossierMel
                            ? [{ '@type': 'DataDownload', encodingFormat: 'application/json', contentUrl: urlStatsEpci(dossierMel) }]
                            : []
                    }
                ]
            });
            document.head.appendChild(script);
        }
    }
};
</script>
