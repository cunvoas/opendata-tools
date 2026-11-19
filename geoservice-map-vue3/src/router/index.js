// https://medium.com/@disjfa/lets-route-a-vue-app-aa9c3f3dbdf8

//import Vue from 'vue';
//import Router from 'vue-router';
import { createWebHistory, createRouter } from "vue-router";
import AppLanding from "../views/AppLanding.vue";
import { features } from "../utils/config.js";
import { routeDepuisParametres } from "../utils/territoires.js";

//Vue.use(Router);

const routes = [
  {
    path: '/',
    name: 'appLanding',
    component: AppLanding,
    meta: {
      title: 'Espaces verts par habitant : Lille et MEL, étude selon l\'OMS',
      description: 'Combien de m² d\'espaces verts par habitant à Lille et dans la Métropole Européenne de Lille (MEL) ? Étude citoyenne : carte, chiffres, méthode, INSEE.',
    },
  },
  {
    path: '/carte',
    name: 'appCarte',
    component: () => import("../views/AppCarte.vue"),
    meta: {
      title: 'Carte des parcs et jardins – MEL | Parcs & Jardins',
      description: 'Explorez la carte interactive des parcs, jardins et espaces verts de la Métropole Européenne de Lille. Analyse de l\'accessibilité selon les critères OMS.',
    },
  },
  {
    path: '/stats',
    name: 'stats',
    component: () => import("../views/AppStatistics.vue"),
    meta: {
      title: 'Statistiques des espaces verts – MEL | Parcs & Jardins',
      description: 'Statistiques détaillées sur la superficie et la répartition des espaces verts par habitant dans la MEL. Comparaison avec les standards OMS (10 m² par habitant).',
    },
  },
  {
    // Une page par intercommunalité, adressée par son numéro Siren (stable après ré-import).
    path: '/stats/epci/:siren',
    name: 'statsEpci',
    component: () => import("../views/AppStatistics.vue"),
  },
  {
    // Une page par commune, adressée par son code officiel géographique (code Insee).
    path: '/stats/:insee',
    name: 'statsCommune',
    component: () => import("../views/AppStatistics.vue"),
  },
  // Écran NDVI derrière le flag VITE_FEATURE_NDVI : sans lui, /ndvi retombe sur la redirection vers /carte.
  ...(features.ndvi ? [{
    path: '/ndvi',
    name: 'ndvi',
    component: () => import("../views/AppNdvi.vue"),
    meta: {
      title: 'Végétation des parcs et jardins – MEL | Parcs & Jardins',
      description: 'Indices de végétation (NDVI, NDRE, GNDVI, SAVI) mesurés par satellite sur les parcs et jardins de la Métropole Européenne de Lille : moyenne, minimum, maximum et écart-type par parc.',
    },
  }] : []),
  {
    path: '/information',
    name: 'appInformation',
    component: () => import("../views/AppInformation.vue"),
    meta: {
      title: 'Informations – Méthodologie et sources | Parcs & Jardins',
      description: 'Méthodologie, sources de données et informations sur l\'étude géographique des parcs et jardins de la MEL réalisée par Aut\'MEL, Deûl\'Air, Entrelianes et LM Oxygène.',
    },
  },
  {
    // Page dédiée : la comparaison avec l'étude de l'INSEE a son URL, l'accueil n'en garde qu'un résumé.
    path: '/etude-insee',
    name: 'etudeInsee',
    component: () => import("../views/AppEtudeInsee.vue"),
    meta: {
      title: 'Étude INSEE et espaces verts : pourquoi les chiffres diffèrent',
      description: 'Pourquoi nos chiffres d\'espaces verts par habitant diffèrent de ceux de l\'INSEE : périmètre, seuils OMS, mesure de l\'accès à pied et sources datées.',
    },
  },
  {
    path: '/aideVideo',
    name: 'appVideoAdmin',
    component: () => import("../views/AppVideoAdmin.vue"),
    meta: {
      title: 'Aide vidéo | Parcs & Jardins',
      description: 'Tutoriels vidéo pour utiliser l\'application cartographique des parcs et jardins de la MEL.',
    },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/carte'
  }
];

const router = createRouter({
  //history: createWebHistory(import.meta.env.VITE_APP_BASE_URL),
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// Anciens liens ?city=… / ?c2c=… : renvoyés vers l'adresse canonique du territoire.
router.beforeEach((to) => {
  if (to.name !== 'stats') return true;
  const cible = routeDepuisParametres(to.query);
  return cible ? { path: cible, replace: true } : true;
});

export default router;
