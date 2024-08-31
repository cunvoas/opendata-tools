// https://medium.com/@disjfa/lets-route-a-vue-app-aa9c3f3dbdf8

import Vue from 'vue';
import Router from 'vue-router';
import AppCarte from "../views/AppCarte.vue";
import AppSurface from "../views/AppSurface.vue";

Vue.use(Router);

const routes = [
  {
    path: '/carte',
    name: 'appCarte',
    title: 'Carte',
    component: AppCarte,
  },
  {
    path: '/surface',
    name: 'appSurface',
    title: 'Surface',
    component: AppSurface,
  },
  {
    path: '/',
    redirect: { name: 'appCarte' },
  },
];


export default new Router({
  routes,
});