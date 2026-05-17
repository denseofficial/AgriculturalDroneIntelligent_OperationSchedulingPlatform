const authHeaders = () => {
  const token = localStorage.getItem('agro_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const request = (url, options = {}) => fetch(url, {
  ...options,
  headers: {
    ...authHeaders(),
    ...(options.headers || {})
  }
}).then(unwrap)

const unwrap = async (response) => {
  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('agro_token')
      localStorage.removeItem('agro_user')
    }
    throw new Error(`接口请求失败：HTTP ${response.status}`)
  }
  const body = await response.json()
  if (body.code !== 200) {
    throw new Error(body.message || '请求失败')
  }
  return body.data
}

export const api = {
  login: (payload) => request('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  overview: () => request('/api/dashboard/overview'),
  drones: () => request('/api/drones'),
  fields: () => request('/api/fields'),
  mapOverview: () => request('/api/map/overview'),
  tasks: () => request('/api/tasks'),
  schedules: () => request('/api/schedules'),
  records: () => request('/api/schedules/records'),
  logs: () => request('/api/logs'),
  saveDrone: (payload) => request('/api/drones', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  deleteDrone: (id) => request(`/api/drones/${id}`, { method: 'DELETE' }),
  saveField: (payload) => request('/api/fields', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  deleteField: (id) => request(`/api/fields/${id}`, { method: 'DELETE' }),
  saveTask: (payload) => request('/api/tasks', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  createTask: (payload) => api.saveTask(payload),
  deleteTask: (id) => request(`/api/tasks/${id}`, { method: 'DELETE' }),
  generate: () => request('/api/schedules/generate', { method: 'POST' }),
  confirmSchedule: (id) => request(`/api/schedules/${id}/confirm`, { method: 'POST' }),
  cancelSchedule: (id) => request(`/api/schedules/${id}/cancel`, { method: 'POST' }),
  startSchedule: (id) => request(`/api/schedules/${id}/start`, { method: 'POST' }),
  completeSchedule: (id, payload) => request(`/api/schedules/${id}/complete`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
}
