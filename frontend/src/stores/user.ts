import { defineStore } from 'pinia';
import { api } from '../api/client';

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: 'u-demo',
    users: [] as any[],
    account: null as any,
  }),
  getters: {
    currentUser(state) {
      return state.users.find((u) => u.userId === state.userId) || null;
    },
    isAdmin(state) {
      const u = state.users.find((x) => x.userId === state.userId);
      return u?.role === 'ADMIN';
    },
  },
  actions: {
    async fetchUsers() {
      this.users = await api.get('/users');
      return this.users;
    },
    switchUser(userId: string) {
      this.userId = userId;
    },
    async fetchAccount() {
      this.account = await api.get(`/points/account?userId=${this.userId}`);
      return this.account;
    },
  },
});
