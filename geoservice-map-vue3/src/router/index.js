// https://medium.com/@disjfa/lets-route-a-vue-app-aa9c3f3dbdf8

//import Vue from 'vue';
//import Router from 'vue-router';
import { createWebHistory, createRouter } from "vue-router";
import AppCarte from "../views/AppCarte.vue";
import AppStatistics from "../views/AppStatistics.vue";
import AppInformation from "../views/AppInformation.vue";
import AppVideoAdmin from "../views/AppVideoAdmin.vue";

//Vue.use(Router);

const routes = [
  {
    path: '/carte',
    name: 'appCarte',
    component: AppCarte,
    meta: {
      title: 'Carte des parcs et jardins – MEL | Parcs & Jardins',
      description: 'Explorez la carte interactive des parcs, jardins et espaces verts de la Métropole Européenne de Lille. Analyse de l\'accessibilité selon les critères OMS.',
    },
  },
  {
    path: '/stats',
    name: 'stats',
    component: AppStatistics,
    meta: {
      title: 'Statistiques des espaces verts – MEL | Parcs & Jardins',
      description: 'Statistiques détaillées sur la superficie et la répartition des espaces verts par habitant dans la MEL. Comparaison avec les standards OMS (10 m² par habitant).',
    },
  },
  {
    path: '/information',
    name: 'appInformation',
    component: AppInformation,
    meta: {
      title: 'Informations – Méthodologie et sources | Parcs & Jardins',
      description: 'Méthodologie, sources de données et informations sur l\'étude géographique des parcs et jardins de la MEL réalisée par Autmel, Deûl\'Air, Entrelianne et LM Oxygène.',
    },
  },
  {
    path: '/aideoVideo',
    name: 'appVideoAdmin',
    component: AppVideoAdmin,
    meta: {
      title: 'Aide vidéo | Parcs & Jardins',
      description: 'Tutoriels vidéo pour utiliser l\'application cartographique des parcs et jardins de la MEL.',
    },
  },
  {
    path: '/',
    redirect: { name: 'appCarte' },
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
