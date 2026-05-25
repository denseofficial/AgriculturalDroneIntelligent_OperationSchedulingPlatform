import UsersPage from './views/UsersPage.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'dashboard' },
  { path: '/map', name: 'map' },
  { path: '/drones', name: 'drones' },
  { path: '/fields', name: 'fields' },
  { path: '/tasks', name: 'tasks' },
  { path: '/users', name: 'users', component: UsersPage },
  { path: '/schedules', name: 'schedules' },
  { path: '/logs', name: 'logs' },
  { path: '/pest-detection', name: 'pest-detection' }
]

export default routes

