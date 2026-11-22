import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  { path: '/', name: 'home', component: () => import('../pages/SongList.vue') },
  { path: '/articles', name: 'articles', component: () => import('../pages/ArticleList.vue') },
  { path: '/articles/:articleId', name: 'article-read', component: () => import('../pages/ArticleRead.vue') },
  { path: '/library', name: 'library', component: () => import('../pages/Library.vue') },
  { path: '/paths', name: 'paths', component: () => import('../pages/Paths.vue') },
  { path: '/placement', name: 'placement', component: () => import('../pages/Placement.vue') },
  { path: '/admin', name: 'admin', component: () => import('../pages/Admin.vue') },
  { path: '/path', name: 'path', component: () => import('../pages/PathPage.vue') },
  { path: '/learn/:platform/:songId', name: 'learn', component: () => import('../pages/Learn.vue') },
  { path: '/points', name: 'points', component: () => import('../pages/PointsCenter.vue') },
  { path: '/mall', name: 'mall', component: () => import('../pages/Mall.vue') },
  { path: '/settlement/:sessionId', name: 'settlement', component: () => import('../pages/Settlement.vue') },
];

export default createRouter({
  history: createWebHistory(),
  routes,
});
