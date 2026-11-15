// https://medium.com/@disjfa/lets-route-a-vue-app-aa9c3f3dbdf8

//import Vue from 'vue';
//import Router from 'vue-router';
import { createWebHistory, createRouter } from "vue-router";
import AppLanding from "../views/AppLanding.vue";
import { features } from "../utils/config.js";

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
  // Écran NDVI derrière le flag VITE_FEATURE_NDVI : sans lui, /ndvi retombe sur la redirection vers /carte.
  ...(features.ndvi ? [{
    path: '/ndvi',
    name: 'ndvi',
    component: () => import("../views/AppNdvi.vue"),
    meta: {
      title: 'NDVI des parcs et jardins – MEL | Parcs & Jardins',
      description: 'Indice de végétation NDVI mesuré par satellite sur les parcs et jardins de la Métropole Européenne de Lille : moyenne, minimum, maximum et écart-type par parc.',
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
    path: '/etude-insee',
    name: 'appEtudeInsee',
    component: () => import("../views/AppEtudeInsee.vue"),
    meta: {
      title: 'Étude de l\'INSEE sur l\'accès aux espaces verts | Parcs & Jardins',
      description: 'Liens vers l\'étude de l\'INSEE sur l\'accès aux espaces verts dans les grands centres urbains et vers sa méthode.',
      // Page sans contenu propre pour l'instant : à indexer une fois la comparaison rédigée.
      robots: 'noindex, follow',
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

export default router;
