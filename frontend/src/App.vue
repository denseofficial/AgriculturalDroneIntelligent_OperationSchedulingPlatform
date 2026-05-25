<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { gsap } from 'gsap'
import { api } from './api'
import { useRouter, useRoute } from 'vue-router'

const loading = ref(false)
const message = ref('')
const activeView = ref('dashboard')
const router = useRouter()
const route = useRoute()
const goUsers = () => { activeView.value = 'users'; router.push('/users') }
const currentUser = ref(JSON.parse(localStorage.getItem('agro_user') || 'null'))
const overview = ref({})
const drones = ref([])
const fields = ref([])
const mapOverview = ref({ fields: [], boundaries: {}, drones: [], tracks: [] })
const tasks = ref([])
const schedules = ref([])
const records = ref([])
const logs = ref([])
const pestFile = ref(null)
const pestPreviewUrl = ref('')
const pestFieldId = ref('')
const pestResult = ref(null)
const pestRecords = ref([])
const pestLoading = ref(false)
const pestFileInputRef = ref(null)
const workspaceBody = ref(null)
const animatedOverview = reactive({
  droneCount: 0,
  idleDroneCount: 0,
  pendingTaskCount: 0,
  todayAreaMu: 0
})
const loginPanel = ref(null)
const loginBrand = ref(null)
const routePath = ref(null)
const dronePulse = ref(null)
const replayDot = ref(null)
const amapContainer = ref(null)
const amapLoaded = ref(false)
const amapError = ref('')
const pickedLngLat = ref(null)
const replayLngLat = ref(null)
const taskStatusFilter = ref('ALL')
const taskPage = ref(1)
const pageSize = 6
const amapKey = import.meta.env.VITE_AMAP_KEY || ''
const amapSecurityCode = import.meta.env.VITE_AMAP_SECURITY_CODE || ''
let amapInstance = null
let AMapApi = null
let amapReplayMarker = null
let replayTween = null

const blankDrone = () => ({
  id: null,
  code: '',
  model: '',
  payloadKg: 20,
  sprayWidthMeter: 7,
  batteryPercent: 80,
  status: 'IDLE',
  longitude: 116.397128,
  latitude: 39.916527
})

const blankField = () => ({
  id: null,
  name: '',
  cropType: '水稻',
  areaMu: 50,
  soilMoistureLevel: 'MEDIUM',
  pestRiskLevel: 'MEDIUM',
  longitude: 116.397128,
  latitude: 39.916527
})

const blankTask = () => ({
  id: null,
  taskNo: '',
  fieldId: '',
  operationType: 'PESTICIDE',
  requiredAreaMu: 50,
  priority: 4,
  earliestStartTime: '',
  latestEndTime: '',
  status: 'PENDING',
  remark: ''
})

const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})
const droneForm = reactive(blankDrone())
const fieldForm = reactive(blankField())
const taskForm = reactive(blankTask())
const showDroneForm = ref(false)
const showFieldForm = ref(false)
const showTaskForm = ref(false)
// users management moved to dedicated route / component

const pendingTasks = computed(() => tasks.value.filter((task) => task.status === 'PENDING'))
const filteredTasks = computed(() => taskStatusFilter.value === 'ALL'
  ? tasks.value
  : tasks.value.filter((task) => task.status === taskStatusFilter.value))
const pagedTasks = computed(() => filteredTasks.value.slice((taskPage.value - 1) * pageSize, taskPage.value * pageSize))
const totalTaskPages = computed(() => Math.max(1, Math.ceil(filteredTasks.value.length / pageSize)))
const pageTitle = computed(() => ({
  dashboard: '作业驾驶舱',
  map: '地图作业态势',
  drones: '无人机管理',
  fields: '地块管理',
  tasks: '任务管理',
  users: '用户管理',
  schedules: '调度计划',
  logs: '操作日志',
  'pest-detection': 'AI虫害检测'
}[activeView.value]))
const chartRows = computed(() => [
  { label: '待调度', value: tasks.value.filter((item) => item.status === 'PENDING').length, color: '#d9842b' },
  { label: '已调度', value: tasks.value.filter((item) => item.status === 'SCHEDULED').length, color: '#236a49' },
  { label: '已生成计划', value: schedules.value.length, color: '#5272a8' }
])
const maxChartValue = computed(() => Math.max(1, ...chartRows.value.map((row) => row.value)))

const fmt = (value) => value ? String(value).replace('T', ' ').slice(0, 16) : '-'
const toInputTime = (value) => value ? String(value).slice(0, 16) : ''
const fieldName = (id) => fields.value.find((field) => field.id === Number(id))?.name || '-'
const statusText = (status) => ({
  IDLE: '空闲',
  WORKING: '作业中',
  CHARGING: '充电中',
  MAINTENANCE: '维护中',
  PENDING: '待调度',
  SCHEDULED: '已调度',
  GENERATED: '已生成',
  CONFIRMED: '已确认',
  RUNNING: '作业中',
  FINISHED: '已完成',
  CANCELED: '已取消'
}[status] || status)
const operationText = (type) => ({
  PESTICIDE: '植保喷洒',
  FERTILIZER: '变量施肥',
  SEEDING: '播撒补种',
  INSPECTION: '巡田监测'
}[type] || type)
const riskText = (level) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[level] || level)
const mapBounds = computed(() => {
  const points = []
  Object.values(mapOverview.value.boundaries || {}).forEach((items) => items.forEach((item) => points.push(item)))
  ;(mapOverview.value.drones || []).forEach((item) => points.push(item))
  ;(mapOverview.value.tracks || []).forEach((item) => points.push(item))
  const longitudes = points.map((item) => Number(item.longitude)).filter(Number.isFinite)
  const latitudes = points.map((item) => Number(item.latitude)).filter(Number.isFinite)
  if (!longitudes.length || !latitudes.length) {
    return { minLon: 116.35, maxLon: 116.43, minLat: 39.90, maxLat: 39.95 }
  }
  return {
    minLon: Math.min(...longitudes),
    maxLon: Math.max(...longitudes),
    minLat: Math.min(...latitudes),
    maxLat: Math.max(...latitudes)
  }
})

const mapPointStyle = (point) => {
  const bounds = mapBounds.value
  const lonRange = bounds.maxLon - bounds.minLon || 0.01
  const latRange = bounds.maxLat - bounds.minLat || 0.01
  return {
    left: `${8 + ((Number(point.longitude) - bounds.minLon) / lonRange) * 84}%`,
    top: `${92 - ((Number(point.latitude) - bounds.minLat) / latRange) * 84}%`
  }
}

const polygonPoints = (points) => points
  .map((point) => {
    const style = mapPointStyle(point)
    return `${parseFloat(style.left)},${parseFloat(style.top)}`
  })
  .join(' ')

const trackPoints = computed(() => (mapOverview.value.tracks || [])
  .map((point) => {
    const style = mapPointStyle(point)
    return `${parseFloat(style.left)},${parseFloat(style.top)}`
  })
  .join(' '))

const trackReplayPoints = computed(() => (mapOverview.value.tracks || [])
  .map((point) => {
    const style = mapPointStyle(point)
    return {
      x: parseFloat(style.left),
      y: parseFloat(style.top),
      longitude: Number(point.longitude),
      latitude: Number(point.latitude),
      batteryPercent: point.batteryPercent
    }
  })
  .filter((point) => Number.isFinite(point.x) && Number.isFinite(point.y)))

const loadAmapScript = () => {
  if (window.AMapLoader) {
    return Promise.resolve()
  }
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://webapi.amap.com/loader.js'
    script.onload = resolve
    script.onerror = () => reject(new Error('高德地图加载器加载失败'))
    document.head.appendChild(script)
  })
}

const amapLngLat = (item) => [Number(item.longitude), Number(item.latitude)]

const drawAmapData = () => {
  if (!amapInstance || !AMapApi) return
  amapInstance.clearMap()
  amapReplayMarker = null

  ;(mapOverview.value.fields || []).forEach((field) => {
    const boundary = mapOverview.value.boundaries?.[field.id] || []
    if (boundary.length >= 3) {
      const polygon = new AMapApi.Polygon({
        path: boundary.map(amapLngLat),
        fillColor: '#a5d66d',
        fillOpacity: 0.28,
        strokeColor: '#236a49',
        strokeWeight: 2
      })
      amapInstance.add(polygon)
    }
    amapInstance.add(new AMapApi.Marker({
      position: amapLngLat(field),
      title: field.name,
      label: { content: field.name, direction: 'top' }
    }))
  })

  ;(mapOverview.value.drones || []).forEach((drone) => {
    amapInstance.add(new AMapApi.Marker({
      position: amapLngLat(drone),
      title: drone.code,
      label: { content: `${drone.code} · ${statusText(drone.status)}`, direction: 'right' }
    }))
  })

  const tracks = mapOverview.value.tracks || []
  if (tracks.length > 1) {
    amapInstance.add(new AMapApi.Polyline({
      path: tracks.map(amapLngLat),
      strokeColor: '#d9842b',
      strokeWeight: 5,
      strokeStyle: 'dashed'
    }))
    amapReplayMarker = new AMapApi.Marker({
      position: amapLngLat(tracks[0]),
      title: '轨迹回放',
      content: '<div class="amap-replay-marker"></div>'
    })
    amapInstance.add(amapReplayMarker)
  }
  const allPoints = [
    ...(mapOverview.value.fields || []),
    ...(mapOverview.value.drones || []),
    ...tracks
  ].filter((item) => item.longitude && item.latitude)
  if (allPoints.length) {
    amapInstance.setFitView()
  }
  startTrackReplay()
}

const initAmap = async () => {
  if (activeView.value !== 'map' || !amapKey || !amapContainer.value) return
  try {
    amapError.value = ''
    if (amapSecurityCode) {
      window._AMapSecurityConfig = { securityJsCode: amapSecurityCode }
    }
    await loadAmapScript()
    if (!AMapApi) {
      AMapApi = await window.AMapLoader.load({
        key: amapKey,
        version: '2.0',
        plugins: ['AMap.Scale', 'AMap.ToolBar']
      })
    }
    if (!amapInstance) {
      amapInstance = new AMapApi.Map(amapContainer.value, {
        viewMode: '2D',
        zoom: 13,
        center: [116.397128, 39.916527]
      })
      amapInstance.addControl(new AMapApi.Scale())
      amapInstance.addControl(new AMapApi.ToolBar())
      amapInstance.on('click', (event) => {
        pickedLngLat.value = {
          longitude: event.lnglat.getLng().toFixed(6),
          latitude: event.lnglat.getLat().toFixed(6)
        }
      })
    }
    amapLoaded.value = true
    drawAmapData()
  } catch (error) {
    amapLoaded.value = false
    amapError.value = error.message
  }
}

const destroyAmap = () => {
  stopTrackReplay()
  if (amapInstance) {
    amapInstance.destroy()
    amapInstance = null
    amapLoaded.value = false
  }
}

const stopTrackReplay = () => {
  if (replayTween) {
    replayTween.kill()
    replayTween = null
  }
}

const startTrackReplay = async () => {
  stopTrackReplay()
  const tracks = mapOverview.value.tracks || []
  const points = trackReplayPoints.value
  if (tracks.length < 2 || points.length < 2) return

  await nextTick()
  const state = { index: 0 }
  replayTween = gsap.to(state, {
    index: tracks.length - 1,
    duration: Math.max(4, tracks.length * 0.75),
    ease: 'none',
    repeat: -1,
    onUpdate: () => {
      const index = Math.min(tracks.length - 1, Math.round(state.index))
      const track = tracks[index]
      const point = points[index]
      replayLngLat.value = {
        longitude: Number(track.longitude).toFixed(6),
        latitude: Number(track.latitude).toFixed(6),
        batteryPercent: track.batteryPercent
      }
      if (replayDot.value && point) {
        gsap.set(replayDot.value, { attr: { cx: point.x, cy: point.y } })
      }
      if (amapReplayMarker && AMapApi) {
        amapReplayMarker.setPosition(amapLngLat(track))
      }
    }
  })
}

const assign = (target, source) => Object.assign(target, source)

const openNewDrone = () => {
  assign(droneForm, blankDrone())
  showDroneForm.value = true
}

const openNewField = () => {
  assign(fieldForm, blankField())
  showFieldForm.value = true
}

const openNewTask = () => {
  assign(taskForm, blankTask())
  if (fields.value.length > 0) {
    taskForm.fieldId = fields.value[0].id
    taskForm.requiredAreaMu = fields.value[0].areaMu
  }
  showTaskForm.value = true
}

const loadAll = async () => {
  if (!currentUser.value) {
    return
  }
  loading.value = true
  message.value = ''
  try {
    const [overviewData, droneData, fieldData, taskData, scheduleData, recordData, logData] = await Promise.all([
      api.overview(),
      api.drones(),
      api.fields(),
      api.tasks(),
      api.schedules(),
      api.records(),
      api.logs()
    ])
    overview.value = overviewData
    drones.value = droneData
    fields.value = fieldData
    tasks.value = taskData
    // users handled by UsersPage component
    schedules.value = scheduleData
    records.value = recordData
    logs.value = logData
    try {
      mapOverview.value = await api.mapOverview()
    } catch (error) {
      mapOverview.value = { fields: fieldData, boundaries: {}, drones: droneData, tracks: [] }
    }
    try {
      pestRecords.value = await api.pestRecords(null)
    } catch (error) {
      // table may not exist yet on first deploy
    }
    if (!taskForm.fieldId && fieldData.length > 0) {
      taskForm.fieldId = fieldData[0].id
      taskForm.requiredAreaMu = fieldData[0].areaMu
    }
    await animateViewEnter()
    await animateDashboard()
  } catch (error) {
    message.value = error.message
  } finally {
    loading.value = false
  }
}

const login = async () => {
  loading.value = true
  message.value = ''
  try {
    const user = await api.login(loginForm)
    localStorage.setItem('agro_token', user.token)
    localStorage.setItem('agro_user', JSON.stringify(user))
    currentUser.value = user
    await loadAll()
  } catch (error) {
    message.value = error.message
  } finally {
    loading.value = false
  }
}

const logout = () => {
  localStorage.removeItem('agro_token')
  localStorage.removeItem('agro_user')
  currentUser.value = null
  message.value = ''
}

const runLoginAnimation = async () => {
  if (currentUser.value) return
  await nextTick()
  const timeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
  timeline
    .fromTo('.login-visual .field-tile', { opacity: 0, y: 18 }, { opacity: 1, y: 0, duration: 0.65, stagger: 0.08 })
    .fromTo(loginPanel.value, { opacity: 0, x: 36, scale: 0.98 }, { opacity: 1, x: 0, scale: 1, duration: 0.75 }, '-=0.45')
    .fromTo(loginBrand.value, { opacity: 0, y: -12 }, { opacity: 1, y: 0, duration: 0.45 }, '-=0.45')
    .fromTo('.login-form label, .login-form button, .login-meta-card', { opacity: 0, y: 14 }, { opacity: 1, y: 0, duration: 0.45, stagger: 0.08 }, '-=0.25')

  if (routePath.value) {
    const length = routePath.value.getTotalLength()
    gsap.set(routePath.value, { strokeDasharray: length, strokeDashoffset: length })
    gsap.to(routePath.value, { strokeDashoffset: 0, duration: 1.8, ease: 'power2.inOut', repeat: -1, repeatDelay: 0.8 })
  }
  if (dronePulse.value) {
    gsap.to(dronePulse.value, {
      keyframes: [
        { attr: { cx: 18, cy: 68 }, duration: 0.8 },
        { attr: { cx: 35, cy: 49 }, duration: 0.8 },
        { attr: { cx: 54, cy: 56 }, duration: 0.8 },
        { attr: { cx: 72, cy: 33 }, duration: 0.8 },
        { attr: { cx: 86, cy: 42 }, duration: 0.8 }
      ],
      repeat: -1,
      ease: 'sine.inOut'
    })
    gsap.to(dronePulse.value, { scale: 1.28, transformOrigin: 'center', yoyo: true, repeat: -1, duration: 0.8, ease: 'sine.inOut' })
  }
}

const animateDashboard = async () => {
  if (activeView.value !== 'dashboard') return
  await nextTick()
  gsap.fromTo('.metric-grid article', { opacity: 0, y: 14 }, { opacity: 1, y: 0, duration: 0.45, stagger: 0.08, ease: 'power2.out' })
  gsap.fromTo('.drone-card', { opacity: 0, y: 10 }, { opacity: 1, y: 0, duration: 0.38, stagger: 0.05, ease: 'power2.out' })
  gsap.to(animatedOverview, {
    droneCount: Number(overview.value.droneCount || 0),
    idleDroneCount: Number(overview.value.idleDroneCount || 0),
    pendingTaskCount: Number(overview.value.pendingTaskCount || 0),
    todayAreaMu: Number(overview.value.todayAreaMu || 0),
    duration: 0.9,
    ease: 'power2.out'
  })
  gsap.fromTo('.chart-row i', { width: '0%' }, {
    width: (index) => `${Math.max(8, (chartRows.value[index]?.value || 0) / maxChartValue.value * 100)}%`,
    duration: 0.8,
    stagger: 0.1,
    ease: 'power2.out'
  })
}

const animateViewEnter = async () => {
  await nextTick()
  gsap.fromTo(workspaceBody.value, { opacity: 0, y: 12 }, { opacity: 1, y: 0, duration: 0.34, ease: 'power2.out' })
  gsap.fromTo('.panel, .metric-grid article', { opacity: 0, y: 14 }, { opacity: 1, y: 0, duration: 0.38, stagger: 0.05, ease: 'power2.out' })
}

const saveDrone = async () => {
  loading.value = true
  try {
    await api.saveDrone({ ...droneForm })
    message.value = droneForm.id ? '无人机已更新' : '无人机已新增'
    assign(droneForm, blankDrone())
    showDroneForm.value = false
    await loadAll()
  } catch (error) {
    message.value = error.message
  } finally {
    loading.value = false
  }
}

const editDrone = (drone) => {
  assign(droneForm, { ...drone })
  showDroneForm.value = true
}
const removeDrone = async (id) => {
  if (!window.confirm('确认删除该无人机？')) return
  await api.deleteDrone(id)
  message.value = '无人机已删除'
  await loadAll()
}

const saveField = async () => {
  loading.value = true
  try {
    await api.saveField({ ...fieldForm })
    message.value = fieldForm.id ? '地块已更新' : '地块已新增'
    assign(fieldForm, blankField())
    showFieldForm.value = false
    await loadAll()
  } catch (error) {
    message.value = error.message
  } finally {
    loading.value = false
  }
}

const editField = (field) => {
  assign(fieldForm, { ...field })
  showFieldForm.value = true
}
const removeField = async (id) => {
  if (!window.confirm('确认删除该地块？已关联任务的地块不能删除。')) return
  await api.deleteField(id)
  message.value = '地块已删除'
  await loadAll()
}

const saveTask = async () => {
  loading.value = true
  try {
    await api.saveTask({
      ...taskForm,
      fieldId: Number(taskForm.fieldId),
      requiredAreaMu: Number(taskForm.requiredAreaMu),
      priority: Number(taskForm.priority)
    })
    message.value = taskForm.id ? '任务已更新' : '任务已创建'
    assign(taskForm, blankTask())
    if (fields.value.length > 0) {
      taskForm.fieldId = fields.value[0].id
      taskForm.requiredAreaMu = fields.value[0].areaMu
    }
    showTaskForm.value = false
    await loadAll()
  } catch (error) {
    message.value = error.message
  } finally {
    loading.value = false
  }
}

// user management moved to UsersPage component

const editTask = (task) => {
  assign(taskForm, {
    ...task,
    earliestStartTime: toInputTime(task.earliestStartTime),
    latestEndTime: toInputTime(task.latestEndTime)
  })
  showTaskForm.value = true
}
const removeTask = async (id) => {
  if (!window.confirm('确认删除该任务？')) return
  await api.deleteTask(id)
  message.value = '任务已删除'
  await loadAll()
}
const useFieldArea = () => {
  const field = fields.value.find((item) => item.id === Number(taskForm.fieldId))
  if (field) taskForm.requiredAreaMu = field.areaMu
}

const generateSchedule = async () => {
  loading.value = true
  try {
    const plans = await api.generate()
    message.value = plans.length ? `已生成 ${plans.length} 条调度方案` : '暂无待调度任务'
    await loadAll()
    taskStatusFilter.value = plans.length ? 'SCHEDULED' : 'ALL'
    taskPage.value = 1
    activeView.value = 'tasks'
  } catch (error) {
    message.value = error.message
  } finally {
    loading.value = false
  }
}

const confirmPlan = async (id) => {
  await api.confirmSchedule(id)
  message.value = '调度计划已确认'
  await loadAll()
}

const startPlan = async (id) => {
  await api.startSchedule(id)
  message.value = '作业已开始'
  await loadAll()
}

const completePlan = async (plan) => {
  const area = window.prompt('请输入实际作业面积（亩）', plan.estimatedAreaMu || '')
  if (area === null) return
  await api.completeSchedule(plan.id, {
    actualAreaMu: Number(area),
    result: 'FINISHED',
    exceptionRemark: ''
  })
  message.value = '作业已完成'
  await loadAll()
}

const cancelPlan = async (id) => {
  if (!window.confirm('确认取消该调度计划？任务会恢复为待调度。')) return
  await api.cancelSchedule(id)
  message.value = '调度计划已取消'
  await loadAll()
}

const exportSchedules = () => {
  const header = ['任务号', '地块', '作业类型', '无人机', '开始时间', '结束时间', '评分', '状态']
  const rows = schedules.value.map((plan) => [
    plan.taskNo,
    plan.fieldName,
    operationText(plan.operationType),
    plan.droneCode,
    fmt(plan.plannedStartTime),
    fmt(plan.plannedEndTime),
    plan.score,
    statusText(plan.status)
  ])
  const csv = [header, ...rows].map((row) => row.join(',')).join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'schedule-plans.csv'
  link.click()
  URL.revokeObjectURL(url)
}

const handlePestFileSelect = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    message.value = '仅支持 JPG/PNG 图片格式'
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    message.value = '文件大小不能超过 10MB'
    return
  }
  pestFile.value = file
  pestPreviewUrl.value = URL.createObjectURL(file)
  pestResult.value = null
}

const detectPest = async () => {
  if (!pestFile.value) {
    message.value = '请先选择图片'
    return
  }
  pestLoading.value = true
  message.value = ''
  try {
    const result = await api.analyzeImage(pestFile.value, pestFieldId.value || null)
    pestResult.value = result
    await loadPestHistory()
    message.value = 'AI分析完成'
  } catch (error) {
    message.value = error.message
  } finally {
    pestLoading.value = false
  }
}

const loadPestHistory = async () => {
  try {
    pestRecords.value = await api.pestRecords(null)
  } catch (error) {
    // silent
  }
}

const applyToField = async (recordId) => {
  if (!window.confirm('确认将此检测结果的风险等级应用到对应地块？')) return
  try {
    await api.applyPestResult(recordId)
    message.value = '已应用到地块'
    await loadPestHistory()
    if (pestResult.value && pestResult.value.id === recordId) {
      pestResult.value.appliedToField = true
    }
  } catch (error) {
    message.value = error.message
  }
}

const deletePestRecord = async (id) => {
  if (!window.confirm('确认删除该检测记录？关联的图片也将被删除。')) return
  try {
    await api.deletePestRecord(id)
    message.value = '记录已删除'
    if (pestResult.value && pestResult.value.id === id) {
      pestResult.value = null
    }
    await loadPestHistory()
  } catch (error) {
    message.value = error.message
  }
}

const resetPestForm = () => {
  pestFile.value = null
  pestPreviewUrl.value = ''
  pestFieldId.value = ''
  pestResult.value = null
  if (pestFileInputRef.value) pestFileInputRef.value.value = ''
}

const severityText = (level) => ({ HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }[level] || level)
const diseaseConfidence = (confidence) => confidence ? (confidence * 100).toFixed(1) + '%' : '-'

const viewPestDetail = (record) => {
  pestResult.value = record
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  loadAll()
  runLoginAnimation()
})

watch(activeView, async (value, oldValue) => {
  await animateViewEnter()
  if (oldValue === 'map' && value !== 'map') {
    destroyAmap()
  }
  if (value === 'map') {
    await nextTick()
    await initAmap()
    if (!amapKey || amapError.value) {
      await startTrackReplay()
    }
  }
  if (value === 'dashboard') {
    await animateDashboard()
  }
})

watch(mapOverview, async () => {
  if (activeView.value === 'map') {
    await nextTick()
    await initAmap()
    if (!amapKey || amapError.value) {
      await startTrackReplay()
    }
  }
}, { deep: true })
</script>

<template>
  <main v-if="!currentUser" class="login-page">
    <section class="login-shell">
      <div class="login-visual">
        <div class="visual-header">
          <span>FIELD OPS / LIVE</span>
          <strong>农田作业态势</strong>
        </div>
        <svg class="login-map" viewBox="0 0 100 100" role="img" aria-label="无人机作业航线">
          <rect class="field-tile tile-a" x="9" y="12" width="24" height="18" rx="2" />
          <rect class="field-tile tile-b" x="42" y="15" width="20" height="22" rx="2" />
          <rect class="field-tile tile-c" x="68" y="18" width="22" height="16" rx="2" />
          <rect class="field-tile tile-d" x="15" y="58" width="25" height="20" rx="2" />
          <rect class="field-tile tile-e" x="53" y="62" width="31" height="19" rx="2" />
          <path id="login-route" ref="routePath" class="login-route" d="M18 68 C28 54, 35 48, 45 51 S62 61, 72 33 S83 35, 86 42" />
          <circle ref="dronePulse" class="drone-pulse" cx="18" cy="68" r="3.2" />
          <circle class="base-point" cx="18" cy="68" r="2.2" />
          <circle class="base-point" cx="86" cy="42" r="2.2" />
        </svg>
        <div class="login-stats">
          <article class="login-meta-card"><span>待调度</span><strong>12</strong></article>
          <article class="login-meta-card"><span>在线无人机</span><strong>08</strong></article>
          <article class="login-meta-card"><span>今日亩数</span><strong>597</strong></article>
        </div>
      </div>

      <section ref="loginPanel" class="login-panel">
        <div ref="loginBrand" class="brand">
          <span class="brand-mark">A</span>
          <div>
            <strong>AgroDrone</strong>
            <small>智能作业调度平台</small>
          </div>
        </div>
        <h1>调度工作台登录</h1>
        <p class="login-tip">默认账号：admin / admin123</p>
        <form class="login-form" @submit.prevent="login">
          <label>用户名<input v-model="loginForm.username" required autocomplete="username" /></label>
          <label>密码<input v-model="loginForm.password" required type="password" autocomplete="current-password" /></label>
          <button class="primary" type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录系统' }}</button>
        </form>
        <p v-if="message" class="message">{{ message }}</p>
      </section>
    </section>
  </main>

  <main v-else class="layout">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">A</span>
        <div>
          <strong>AgroDrone</strong>
          <small>智能作业调度平台</small>
        </div>
      </div>
      <nav>
        <button :class="{ active: activeView === 'dashboard' }" @click="activeView = 'dashboard'">作业驾驶舱</button>
        <button :class="{ active: activeView === 'map' }" @click="activeView = 'map'">地图态势</button>
        <button :class="{ active: activeView === 'drones' }" @click="activeView = 'drones'">无人机管理</button>
        <button :class="{ active: activeView === 'fields' }" @click="activeView = 'fields'">地块管理</button>
        <button :class="{ active: activeView === 'tasks' }" @click="activeView = 'tasks'">任务管理</button>
        <button :class="{ active: activeView === 'users' }" @click="goUsers">用户管理</button>
        <button :class="{ active: activeView === 'schedules' }" @click="activeView = 'schedules'">调度计划</button>
        <button :class="{ active: activeView === 'logs' }" @click="activeView = 'logs'">操作日志</button>
        <button :class="{ active: activeView === 'pest-detection' }" @click="activeView = 'pest-detection'">AI虫害检测</button>
      </nav>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">农业无人机智能作业调度平台</p>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="top-actions">
          <span class="user-badge">{{ currentUser.realName }} / {{ currentUser.role }}</span>
          <button class="ghost" type="button" @click="logout">退出</button>
          <button class="primary" :disabled="loading" @click="generateSchedule">
            {{ loading ? '处理中...' : '生成调度方案' }}
          </button>
        </div>
      </header>

      <p v-if="message" class="message">{{ message }}</p>

      <div ref="workspaceBody" class="workspace-body">
      <template v-if="activeView === 'dashboard'">
        <section class="metric-grid">
          <article><span>无人机总数</span><strong>{{ Math.round(animatedOverview.droneCount) }}</strong></article>
          <article><span>空闲无人机</span><strong>{{ Math.round(animatedOverview.idleDroneCount) }}</strong></article>
          <article><span>待调度任务</span><strong>{{ Math.round(animatedOverview.pendingTaskCount) }}</strong></article>
          <article><span>今日计划亩数</span><strong>{{ Math.round(animatedOverview.todayAreaMu) }}</strong></article>
        </section>

        <section class="content-grid">
          <div class="panel">
            <div class="section-title"><h2>无人机资源池</h2></div>
            <div v-if="drones.length" class="drone-list">
              <article v-for="drone in drones" :key="drone.id" class="drone-card">
                <div><strong>{{ drone.code }}</strong><span>{{ drone.model }}</span></div>
                <div class="battery"><span :style="{ width: drone.batteryPercent + '%' }"></span></div>
                <footer><span>{{ drone.batteryPercent }}%</span><b>{{ statusText(drone.status) }}</b></footer>
              </article>
            </div>
            <p v-else class="empty">暂无无人机数据</p>
          </div>

          <div class="panel">
            <div class="section-title"><h2>任务状态统计</h2></div>
            <div class="chart">
              <div v-for="row in chartRows" :key="row.label" class="chart-row">
                <span>{{ row.label }}</span>
                <div><i :style="{ width: Math.max(8, row.value / maxChartValue * 100) + '%', background: row.color }"></i></div>
                <b>{{ row.value }}</b>
              </div>
            </div>
          </div>
        </section>

      </template>

      <template v-if="activeView === 'users'">
        <router-view />
      </template>

      <template v-if="activeView === 'map'">
        <section class="panel">
          <div class="section-title">
            <h2>地图作业态势</h2>
            <span>{{ amapKey ? '高德地图 / 经纬度查看' : '未配置高德 Key，使用内置地图' }}</span>
          </div>
          <div v-if="amapKey" ref="amapContainer" class="amap-view"></div>
          <p v-if="amapKey && amapError" class="message">{{ amapError }}，已使用内置地图兜底。</p>
          <div v-if="!amapKey || amapError" class="geo-map">
            <svg class="geo-layer" viewBox="0 0 100 100" preserveAspectRatio="none">
              <polygon
                v-for="field in mapOverview.fields"
                :key="field.id"
                :points="polygonPoints(mapOverview.boundaries[field.id] || [])"
                class="field-polygon"
              />
              <polyline v-if="trackPoints" :points="trackPoints" class="track-line" />
              <circle
                v-if="trackReplayPoints.length"
                ref="replayDot"
                class="track-replay-dot"
                :cx="trackReplayPoints[0].x"
                :cy="trackReplayPoints[0].y"
                r="1.6"
              />
            </svg>
            <span
              v-for="field in mapOverview.fields"
              :key="'field-' + field.id"
              class="field-label"
              :style="mapPointStyle(field)"
            >
              {{ field.name }}
            </span>
            <span
              v-for="drone in mapOverview.drones"
              :key="'drone-' + drone.id"
              class="drone-marker"
              :style="mapPointStyle(drone)"
            >
              {{ drone.code }} · {{ statusText(drone.status) }}
            </span>
          </div>
          <div class="coord-panel">
            <strong>经纬度查看</strong>
            <span v-if="replayLngLat">轨迹回放：经度 {{ replayLngLat.longitude }}，纬度 {{ replayLngLat.latitude }}，电量 {{ replayLngLat.batteryPercent }}%</span>
            <span v-else-if="pickedLngLat">经度 {{ pickedLngLat.longitude }}，纬度 {{ pickedLngLat.latitude }}</span>
            <span v-else>在高德地图上点击任意位置即可读取经纬度。</span>
          </div>
          <div class="map-legend">
            <span><i class="legend-field"></i>地块边界</span>
            <span><i class="legend-drone"></i>无人机位置</span>
            <span><i class="legend-track"></i>作业轨迹</span>
          </div>
        </section>
      </template>

      <template v-if="activeView === 'drones'">
        <section v-if="showDroneForm" class="panel">
          <div class="section-title">
            <h2>{{ droneForm.id ? '编辑无人机' : '新增无人机' }}</h2>
            <button class="ghost" type="button" @click="showDroneForm = false">关闭</button>
          </div>
          <form class="task-form" @submit.prevent="saveDrone">
            <label>编号<input v-model="droneForm.code" required /></label>
            <label>型号<input v-model="droneForm.model" required /></label>
            <label>载荷 kg<input v-model.number="droneForm.payloadKg" min="1" type="number" /></label>
            <label>喷幅 m<input v-model.number="droneForm.sprayWidthMeter" min="1" type="number" /></label>
            <label>电量 %<input v-model.number="droneForm.batteryPercent" max="100" min="0" type="number" /></label>
            <label>状态<select v-model="droneForm.status"><option value="IDLE">空闲</option><option value="WORKING">作业中</option><option value="CHARGING">充电中</option><option value="MAINTENANCE">维护中</option></select></label>
            <button class="secondary" type="submit">{{ droneForm.id ? '更新无人机' : '新增无人机' }}</button>
            <button class="ghost" type="button" @click="openNewDrone">清空</button>
          </form>
        </section>
        <section class="panel">
          <div class="section-title">
            <h2>无人机列表</h2>
            <div class="title-actions">
              <span>{{ drones.length }} 架</span>
              <button class="secondary" type="button" @click="openNewDrone">新增无人机</button>
            </div>
          </div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>编号</th><th>型号</th><th>载荷</th><th>喷幅</th><th>电量</th><th>状态</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="drone in drones" :key="drone.id">
                  <td>{{ drone.code }}</td><td>{{ drone.model }}</td><td>{{ drone.payloadKg }} kg</td><td>{{ drone.sprayWidthMeter }} m</td><td>{{ drone.batteryPercent }}%</td><td><span class="tag">{{ statusText(drone.status) }}</span></td>
                  <td><button class="link-btn" @click="editDrone(drone)">编辑</button><button class="danger-btn" @click="removeDrone(drone.id)">删除</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>

      <template v-if="activeView === 'fields'">
        <section v-if="showFieldForm" class="panel">
          <div class="section-title">
            <h2>{{ fieldForm.id ? '编辑地块' : '新增地块' }}</h2>
            <button class="ghost" type="button" @click="showFieldForm = false">关闭</button>
          </div>
          <form class="task-form" @submit.prevent="saveField">
            <label>地块名称<input v-model="fieldForm.name" required /></label>
            <label>作物类型<input v-model="fieldForm.cropType" required /></label>
            <label>面积 亩<input v-model.number="fieldForm.areaMu" min="1" type="number" /></label>
            <label>土壤墒情<select v-model="fieldForm.soilMoistureLevel"><option value="LOW">低</option><option value="MEDIUM">中</option><option value="HIGH">高</option></select></label>
            <label>虫害风险<select v-model="fieldForm.pestRiskLevel"><option value="LOW">低</option><option value="MEDIUM">中</option><option value="HIGH">高</option></select></label>
            <label>经度<input v-model.number="fieldForm.longitude" step="0.000001" type="number" /></label>
            <label>纬度<input v-model.number="fieldForm.latitude" step="0.000001" type="number" /></label>
            <button class="secondary" type="submit">{{ fieldForm.id ? '更新地块' : '新增地块' }}</button>
            <button class="ghost" type="button" @click="openNewField">清空</button>
          </form>
        </section>
        <section class="panel">
          <div class="section-title">
            <h2>地块列表</h2>
            <div class="title-actions">
              <span>{{ fields.length }} 块</span>
              <button class="secondary" type="button" @click="openNewField">新增地块</button>
            </div>
          </div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>名称</th><th>作物</th><th>面积</th><th>墒情</th><th>风险</th><th>位置</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="field in fields" :key="field.id">
                  <td>{{ field.name }}</td><td>{{ field.cropType }}</td><td>{{ field.areaMu }} 亩</td><td>{{ riskText(field.soilMoistureLevel) }}</td><td>{{ riskText(field.pestRiskLevel) }}</td><td>{{ field.longitude }}, {{ field.latitude }}</td>
                  <td><button class="link-btn" @click="editField(field)">编辑</button><button class="danger-btn" @click="removeField(field.id)">删除</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>

      <template v-if="activeView === 'tasks'">
        <section v-if="showTaskForm" class="panel">
          <div class="section-title">
            <h2>{{ taskForm.id ? '编辑作业任务' : '新建作业任务' }}</h2>
            <button class="ghost" type="button" @click="showTaskForm = false">关闭</button>
          </div>
          <form class="task-form" @submit.prevent="saveTask">
            <label>地块<select v-model="taskForm.fieldId" required @change="useFieldArea"><option v-for="field in fields" :key="field.id" :value="field.id">{{ field.name }} / {{ field.cropType }} / {{ field.areaMu }}亩</option></select></label>
            <label>作业类型<select v-model="taskForm.operationType"><option value="PESTICIDE">植保喷洒</option><option value="FERTILIZER">变量施肥</option><option value="SEEDING">播撒补种</option><option value="INSPECTION">巡田监测</option></select></label>
            <label>作业面积<input v-model.number="taskForm.requiredAreaMu" min="1" step="0.5" type="number" /></label>
            <label>优先级<input v-model.number="taskForm.priority" max="5" min="1" type="number" /></label>
            <label>最早开始<input v-model="taskForm.earliestStartTime" type="datetime-local" /></label>
            <label>最晚完成<input v-model="taskForm.latestEndTime" type="datetime-local" /></label>
            <label>状态<select v-model="taskForm.status"><option value="PENDING">待调度</option><option value="SCHEDULED">已调度</option></select></label>
            <label class="wide">备注<input v-model="taskForm.remark" placeholder="例如：虫害预警、雨前窗口、试验田优先" /></label>
            <button class="secondary" type="submit">{{ taskForm.id ? '更新任务' : '保存任务' }}</button>
            <button class="ghost" type="button" @click="openNewTask">清空</button>
          </form>
        </section>
        <section class="panel">
          <div class="section-title">
            <h2>任务列表</h2>
            <div class="title-actions">
              <select v-model="taskStatusFilter" @change="taskPage = 1"><option value="ALL">全部</option><option value="PENDING">待调度</option><option value="SCHEDULED">已调度</option></select>
              <button class="secondary" type="button" @click="openNewTask">新增任务</button>
            </div>
          </div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>任务号</th><th>地块</th><th>类型</th><th>面积</th><th>优先级</th><th>窗口</th><th>状态</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="task in pagedTasks" :key="task.id">
                  <td>{{ task.taskNo }}</td><td>{{ fieldName(task.fieldId) }}</td><td>{{ operationText(task.operationType) }}</td><td>{{ task.requiredAreaMu }} 亩</td><td>{{ task.priority }}</td><td>{{ fmt(task.earliestStartTime) }} - {{ fmt(task.latestEndTime) }}</td><td><span class="tag">{{ statusText(task.status) }}</span></td>
                  <td><button class="link-btn" @click="editTask(task)">编辑</button><button class="danger-btn" @click="removeTask(task.id)">删除</button></td>
                </tr>
              </tbody>
            </table>
            <p v-if="!pagedTasks.length" class="empty">暂无任务数据</p>
          </div>
          <div class="pager"><button :disabled="taskPage === 1" @click="taskPage--">上一页</button><span>{{ taskPage }} / {{ totalTaskPages }}</span><button :disabled="taskPage === totalTaskPages" @click="taskPage++">下一页</button></div>
        </section>
      </template>

      <template v-if="activeView === 'schedules'">
        <section class="panel">
          <div class="section-title"><h2>调度结果</h2><button class="ghost" @click="exportSchedules">导出 CSV</button></div>
          <div v-if="schedules.length" class="schedule-list">
            <article v-for="plan in schedules" :key="plan.id" class="schedule-card">
              <header><strong>{{ plan.taskNo }}</strong><span>{{ plan.score }} 分</span></header>
              <p>{{ plan.fieldName }} / {{ operationText(plan.operationType) }} / {{ plan.droneCode }}</p>
              <div class="time">{{ fmt(plan.plannedStartTime) }} - {{ fmt(plan.plannedEndTime) }}</div>
              <small>{{ plan.decisionReason }}</small>
              <footer class="card-actions">
                <span class="tag">{{ statusText(plan.status) }}</span>
                <button class="link-btn" :disabled="plan.status === 'CONFIRMED'" @click="confirmPlan(plan.id)">确认</button>
                <button class="link-btn" :disabled="plan.status !== 'CONFIRMED'" @click="startPlan(plan.id)">开始</button>
                <button class="link-btn" :disabled="plan.status !== 'RUNNING'" @click="completePlan(plan)">完成</button>
                <button class="danger-btn" :disabled="plan.status === 'CANCELED'" @click="cancelPlan(plan.id)">取消</button>
              </footer>
            </article>
          </div>
          <p v-else class="empty">暂无调度计划，点击右上角生成调度方案。</p>
        </section>

        <section class="panel">
          <div class="section-title"><h2>作业执行记录</h2><span>{{ records.length }} 条</span></div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>任务号</th><th>地块</th><th>无人机</th><th>开始</th><th>完成</th><th>实际面积</th><th>耗时</th><th>结果</th></tr></thead>
              <tbody>
                <tr v-for="record in records" :key="record.id">
                  <td>{{ record.taskNo }}</td>
                  <td>{{ record.fieldName }}</td>
                  <td>{{ record.droneCode }}</td>
                  <td>{{ fmt(record.actualStartTime) }}</td>
                  <td>{{ fmt(record.actualEndTime) }}</td>
                  <td>{{ record.actualAreaMu || '-' }} 亩</td>
                  <td>{{ record.durationMinutes || '-' }} 分钟</td>
                  <td><span class="tag">{{ record.result }}</span></td>
                </tr>
              </tbody>
            </table>
            <p v-if="!records.length" class="empty">暂无作业执行记录。</p>
          </div>
        </section>
      </template>

      <template v-if="activeView === 'logs'">
        <section class="panel">
          <div class="section-title"><h2>操作日志</h2><span>最近 {{ logs.length }} 条</span></div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>时间</th><th>模块</th><th>动作</th><th>对象</th><th>内容</th></tr></thead>
              <tbody>
                <tr v-for="log in logs" :key="log.id">
                  <td>{{ fmt(log.createdAt) }}</td>
                  <td>{{ log.moduleName }}</td>
                  <td><span class="tag">{{ log.actionName }}</span></td>
                  <td>{{ log.targetType }} #{{ log.targetId }}</td>
                  <td>{{ log.content }}</td>
                </tr>
              </tbody>
            </table>
            <p v-if="!logs.length" class="empty">暂无操作日志。</p>
          </div>
        </section>
      </template>

      <template v-if="activeView === 'pest-detection'">
        <section class="panel">
          <div class="section-title">
            <h2>上传农田照片进行AI分析</h2>
            <span>支持 JPG / PNG，最大 10MB</span>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:18px;">
            <div>
              <input ref="pestFileInputRef" type="file" accept="image/jpeg,image/png" @change="handlePestFileSelect" style="display:none" />
              <div @click="$refs.pestFileInputRef.click()" style="border:2px dashed #cbd8ce;border-radius:8px;padding:40px;text-align:center;cursor:pointer;background:#fbfdfb;min-height:220px;display:grid;place-items:center;">
                <img v-if="pestPreviewUrl" :src="pestPreviewUrl" style="max-width:100%;max-height:280px;border-radius:6px;object-fit:contain;" />
                <div v-else>
                  <p style="font-size:36px;margin:0 0 8px;color:#a5d66d;">+</p>
                  <p style="color:#65756a;margin:0;">点击选择农田照片</p>
                </div>
              </div>
              <label style="margin-top:12px;display:grid;gap:6px;">
                <span>关联地块（可选）</span>
                <select v-model="pestFieldId">
                  <option value="">不关联地块</option>
                  <option v-for="field in fields" :key="field.id" :value="field.id">{{ field.name }} / {{ field.cropType }}</option>
                </select>
              </label>
              <div class="card-actions" style="margin-top:14px;">
                <button class="secondary" :disabled="pestLoading || !pestFile" @click="detectPest">
                  {{ pestLoading ? '分析中...' : '开始AI分析' }}
                </button>
                <button class="ghost" @click="resetPestForm">重置</button>
              </div>
            </div>
            <div>
              <div v-if="pestResult" style="border:1px solid #d8e3da;border-radius:8px;padding:18px;background:#ffffff;min-height:260px;">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
                  <h2 style="margin:0;font-size:18px;">分析结果</h2>
                  <span :class="['tag', pestResult.severity === 'HIGH' ? 'tag-danger' : pestResult.severity === 'MEDIUM' ? 'tag-warn' : 'tag-safe']">
                    {{ severityText(pestResult.severity) }}
                  </span>
                </div>
                <table style="min-width:auto;width:100%;">
                  <tbody>
                    <tr><td style="color:#65756a;padding:6px 8px;">检测病害</td><td style="padding:6px 8px;"><strong>{{ pestResult.detectedDisease }}</strong></td></tr>
                    <tr><td style="color:#65756a;padding:6px 8px;">置信度</td><td style="padding:6px 8px;"><strong>{{ diseaseConfidence(pestResult.confidence) }}</strong></td></tr>
                    <tr><td style="color:#65756a;padding:6px 8px;">风险等级</td><td style="padding:6px 8px;">{{ severityText(pestResult.severity) }}</td></tr>
                    <tr><td style="color:#65756a;padding:6px 8px;vertical-align:top;">分析描述</td><td style="padding:6px 8px;white-space:normal;">{{ pestResult.description }}</td></tr>
                  </tbody>
                </table>
                <div style="margin-top:12px;">
                  <p style="font-size:13px;color:#65756a;margin:0 0 6px;">颜色分布</p>
                  <div class="chart">
                    <div class="chart-row" v-for="c in [{l:'健康绿色',k:'greenRatio',c:'#6db04b'},{l:'黄化',k:'yellowRatio',c:'#d9b84a'},{l:'坏死',k:'brownRatio',c:'#8b5e3c'},{l:'真菌/白粉',k:'grayRatio',c:'#aab2b0'},{l:'锈病',k:'orangeRatio',c:'#d9842b'}]" :key="c.k">
                      <span>{{ c.l }}</span>
                      <div><i :style="{width:Math.max(2, (pestResult[c.k]||0)*100)+'%',background:c.color}"></i></div>
                      <b>{{ ((pestResult[c.k]||0)*100).toFixed(1) }}%</b>
                    </div>
                  </div>
                </div>
                <div class="card-actions" style="margin-top:14px;border-top:1px solid #edf1ee;padding-top:14px;">
                  <button class="link-btn" :disabled="!pestResult.fieldPlotId || pestResult.appliedToField" @click="applyToField(pestResult.id)">
                    {{ pestResult.appliedToField ? '已应用到地块' : '应用到地块风险等级' }}
                  </button>
                  <button class="danger-btn" @click="deletePestRecord(pestResult.id)">删除</button>
                </div>
              </div>
              <div v-else style="border:1px dashed #c6d4ca;border-radius:8px;padding:40px;text-align:center;color:#66766b;background:#fbfdfb;min-height:260px;display:grid;place-items:center;">
                <p>上传图片并点击分析，结果将在此处显示</p>
              </div>
            </div>
          </div>
        </section>

        <section class="panel">
          <div class="section-title">
            <h2>检测历史记录</h2>
            <span>{{ pestRecords.length }} 条</span>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>图片</th>
                  <th>检测病害</th>
                  <th>风险等级</th>
                  <th>置信度</th>
                  <th>关联地块</th>
                  <th>已应用</th>
                  <th>检测时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="record in pestRecords" :key="record.id">
                  <td><img :src="record.imageUrl" style="width:48px;height:48px;object-fit:cover;border-radius:4px;cursor:pointer;" @click="viewPestDetail(record)" /></td>
                  <td>{{ record.detectedDisease }}</td>
                  <td><span :class="['tag', record.severity === 'HIGH' ? 'tag-danger' : record.severity === 'MEDIUM' ? 'tag-warn' : 'tag-safe']">{{ severityText(record.severity) }}</span></td>
                  <td>{{ diseaseConfidence(record.confidence) }}</td>
                  <td>{{ fieldName(record.fieldPlotId) }}</td>
                  <td>{{ record.appliedToField ? '是' : '否' }}</td>
                  <td>{{ fmt(record.createdAt) }}</td>
                  <td>
                    <button class="link-btn" @click="viewPestDetail(record)" v-if="!pestResult || pestResult.id !== record.id">查看</button>
                    <button class="link-btn" :disabled="!record.fieldPlotId || record.appliedToField" @click="applyToField(record.id)">
                      {{ record.appliedToField ? '已应用' : '应用' }}
                    </button>
                    <button class="danger-btn" @click="deletePestRecord(record.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <p v-if="!pestRecords.length" class="empty">暂无检测记录，上传图片开始第一次分析。</p>
          </div>
        </section>
      </template>
      </div>
    </section>
  </main>
</template>
