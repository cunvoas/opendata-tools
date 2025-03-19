// https://medium.com/@disjfa/lets-route-a-vue-app-aa9c3f3dbdf8

//import Vue from 'vue';
//import Router from 'vue-router';
import { createWebHistory, createRouter } from "vue-router";
import AppCarte from "../views/AppCarte.vue";
import AppStatistics from "../views/AppStatistics.vue";
import AppInformation from "../views/AppInformation.vue";

//Vue.use(Router);

const routes = [
  {
    path: '/carte',
    name: 'appCarte',
    title: 'Carte',
    component: AppCarte,
  },
  
  {
    path: '/stats',
    name: 'stats',
    title: 'Statistiques',
    component: AppStatistics,
  },
  {
    path: '/information',
    name: 'appInformation',
    title: 'Informations',
    component: AppInformation,
  },
  {
    path: '/',
    redirect: { name: 'appCarte' },
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

export default router;
