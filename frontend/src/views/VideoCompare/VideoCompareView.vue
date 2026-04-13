<template>
  <div class="video-compare-container">
    <h2>健身动作视频对比 - AI 姿态检测</h2>
    
    <div class="action-select">
      <el-select v-model="selectedActionId" placeholder="选择训练动作" @change="onActionChange">
        <el-option
          v-for="action in trainActions"
          :key="action.id"
          :label="action.name"
          :value="action.id">
          <span>{{ action.name }}</span>
          <span class="muscle-tag">{{ action.muscleGroup }}</span>
        </el-option>
      </el-select>
      <el-button type="primary" @click="startPoseDetection" :disabled="!userVideoUrl" style="margin-left: 10px">
        {{ isDetecting ? '检测中...' : '开始 AI 检测' }}
      </el-button>
    </div>

    <div class="video-container" v-if="selectedActionId">
      <div class="video-panel" ref="stdPanelRef">
        <h4>标准动作 <span v-if="stdActionName" class="action-name">({{ stdActionName }})</span></h4>
        <div class="video-wrapper">
          <video 
            ref="stdVideoRef" 
            :src="stdVideoUrl" 
            crossorigin="anonymous"
            @loadedmetadata="onStdVideoLoaded"
            @timeupdate="onTimeUpdate"
            @play="syncPlay"
            @pause="syncPause"
          ></video>
          <canvas ref="stdCanvasRef" class="annotation-canvas"></canvas>
        </div>
        <div class="annotation-legend">
          <span class="legend-item correct">● 绿色 = 正确</span>
          <span class="legend-item warning">● 黄色 = 临界</span>
          <span class="legend-item error">● 红色 = 错误</span>
        </div>
        <div class="keypoint-info" v-if="showAnnotations">
          <div v-for="kp in displayKeypoints" :key="kp.type" class="kp-item" :style="{ color: kp.color }">
            <span class="kp-type">{{ getKeypointLabel(kp.type) }}</span>
            <span class="kp-angle">{{ kp.currentAngle }}°</span>
          </div>
        </div>
      </div>
      
      <div class="video-panel" ref="userPanelRef">
        <h4>我的动作 <span v-if="isDetecting" class="detecting-badge">AI 检测中</span></h4>
        <div class="video-wrapper">
          <video 
            ref="userVideoRef" 
            :src="userVideoUrl" 
            crossorigin="anonymous"
            @loadedmetadata="onUserVideoLoaded"
            @timeupdate="onTimeUpdate"
            @play="syncPlay"
            @pause="syncPause"
          ></video>
          <canvas ref="userCanvasRef" class="annotation-canvas"></canvas>
        </div>
        <div class="annotation-legend">
          <span class="legend-item correct">● 绿色 = 正确</span>
          <span class="legend-item warning">● 黄色 = 临界</span>
          <span class="legend-item error">● 红色 = 错误</span>
        </div>
        <div class="keypoint-info" v-if="showAnnotations && detectedKeypoints.length > 0">
          <div v-for="kp in detectedKeypoints" :key="kp.type + kp.side" class="kp-item" :style="{ color: kp.color }">
            <span class="kp-type">{{ kp.side }}{{ getKeypointLabel(kp.type) }}</span>
            <span class="kp-angle">{{ kp.currentAngle }}°</span>
            <span class="kp-range" v-if="kp.deviation !== undefined">
              偏差: {{ kp.deviation > 0 ? '+' : '' }}{{ kp.deviation }}°
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="playback-controls" v-if="selectedActionId">
      <el-button @click="playBoth" type="primary">▶ 播放</el-button>
      <el-button @click="pauseBoth">⏸ 暂停</el-button>
      <el-button @click="resetBoth">⏮ 重置</el-button>
      <el-button @click="toggleAnnotations">{{ showAnnotations ? '隐藏标注' : '显示标注' }}</el-button>
      <span class="speed-control">
        倍速:
        <el-select v-model="playbackSpeed" @change="setSpeed" size="small">
          <el-option label="0.5x" :value="0.5"></el-option>
          <el-option label="1x" :value="1"></el-option>
          <el-option label="1.5x" :value="1.5"></el-option>
          <el-option label="2x" :value="2"></el-option>
        </el-select>
      </span>
      <span class="time-display" v-if="duration > 0">
        {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
      </span>
    </div>

    <div class="progress-bar" v-if="selectedActionId && duration > 0">
      <el-slider 
        v-model="seekPosition" 
        :min="0" 
        :max="100" 
        :format-tooltip="formatSeekTooltip"
        @change="onSeek"
      />
    </div>

    <div class="upload-section">
      <h3>上传我的动作视频</h3>
      <el-upload
        ref="uploadRef"
        action="/api/video-compare/upload"
        :headers="{ 'X-User-Id': String(userId) }"
        :data="{ actionId: selectedActionId }"
        :before-upload="beforeUpload"
        :on-success="onUploadSuccess"
        :on-error="onUploadError"
        :on-progress="onUploadProgress"
        accept="video/*"
        :auto-upload="false"
      >
        <template #trigger>
          <el-button type="primary">选择视频</el-button>
        </template>
        <el-button type="success" @click="submitUpload" style="margin-left: 10px" :loading="uploading">
          上传到服务器
        </el-button>
        <template #tip>
          <div class="el-upload__tip">支持 MP4、WebM 等格式，最大 500MB</div>
        </template>
      </el-upload>
      <el-progress 
        v-if="uploadProgress > 0" 
        :percentage="uploadProgress" 
        :status="uploadProgress === 100 ? 'success' : ''"
        style="margin-top: 10px"
      />
    </div>

    <div class="history-section">
      <h3>对比历史</h3>
      <el-table :data="compareHistory" stripe v-loading="historyLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="overallScore" label="评分" width="120">
          <template #default="{ row }">
            <el-tag :type="getScoreType(row.overallScore)" size="large">
              {{ row.overallScore || '待评分' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useMediaPipePose, POSE_LANDMARKS } from '@/composables/useMediaPipePose'

const userId = ref(1)
const selectedActionId = ref<number | null>(null)
const trainActions = ref<any[]>([])
const stdVideoUrl = ref('')
const userVideoUrl = ref('')
const compareHistory = ref<any[]>([])
const playbackSpeed = ref(1)
const currentTime = ref(0)
const duration = ref(0)
const seekPosition = ref(0)
const showAnnotations = ref(true)
const uploadProgress = ref(0)
const uploading = ref(false)
const historyLoading = ref(false)
const annotations = ref<any[]>([])
const displayKeypoints = ref<any[]>([])
const detectedKeypoints = ref<any[]>([])
const isDetecting = ref(false)

const stdVideoRef = ref<HTMLVideoElement | null>(null)
const userVideoRef = ref<HTMLVideoElement | null>(null)
const stdCanvasRef = ref<HTMLCanvasElement | null>(null)
const userCanvasRef = ref<HTMLCanvasElement | null>(null)
const stdPanelRef = ref<HTMLElement | null>(null)
const uploadRef = ref<any>(null)

const { landmarks, initialize, destroy, calculateKeypointAngles } = useMediaPipePose()

const stdActionName = computed(() => {
  const action = trainActions.value.find((a: any) => a.id === selectedActionId.value)
  return action ? action.name : ''
})

const keypointLabels: Record<string, string> = {
  left_shoulder: '左肩', right_shoulder: '右肩',
  left_elbow: '左肘', right_elbow: '右肘',
  left_hip: '左髋', right_hip: '右髋',
  left_knee: '左膝', right_knee: '右膝',
  shoulder: '肩', elbow: '肘', hip: '髋', knee: '膝'
}

const getKeypointLabel = (type: string) => keypointLabels[type] || type

const loadTrainActions = async () => {
  try {
    const response = await axios.get('/api/train/actions')
    if (response.data.code === 0) {
      trainActions.value = response.data.data || []
    }
  } catch {
    trainActions.value = [
      { id: 1, name: '杠铃卧推', muscleGroup: 'chest' },
      { id: 4, name: '杠铃深蹲', muscleGroup: 'leg' },
      { id: 5, name: '平板支撑', muscleGroup: 'core' }
    ]
  }
}

const getDefaultAnnotations = (actionId: number) => {
  if (actionId === 1) {
    return [
      { keypointType: 'shoulder', xRatio: 0.35, yRatio: 0.3, standardAngle: 45, angleTolerance: 15, showArc: 1 },
      { keypointType: 'elbow', xRatio: 0.3, yRatio: 0.45, standardAngle: 90, angleTolerance: 20, showArc: 1 }
    ]
  }
  return [
    { keypointType: 'knee', xRatio: 0.5, yRatio: 0.55, standardAngle: 90, angleTolerance: 25, showArc: 1 },
    { keypointType: 'hip', xRatio: 0.5, yRatio: 0.45, standardAngle: 90, angleTolerance: 20, showArc: 1 }
  ]
}

const loadAnnotations = async (actionId: number) => {
  try {
    const response = await axios.get(`/api/video-compare/annotations/${actionId}`)
    if (response.data.code === 0) {
      annotations.value = response.data.data || []
    }
  } catch {
    annotations.value = getDefaultAnnotations(actionId)
  }
  updateDisplayKeypoints()
}

const updateDisplayKeypoints = () => {
  displayKeypoints.value = annotations.value.map((ann: any) => {
    const timePhase = duration.value > 0 ? (currentTime.value / duration.value) % 1 : 0
    const simulatedAngle = ann.standardAngle + Math.sin(timePhase * Math.PI * 4) * ann.angleTolerance * 0.5
    const deviation = Math.abs(simulatedAngle - ann.standardAngle)
    let color = '#67c23a'
    if (deviation > ann.angleTolerance) color = '#f56c6c'
    else if (deviation > ann.angleTolerance * 0.6) color = '#e6a23c'
    return { type: ann.keypointType, currentAngle: Math.round(simulatedAngle), color }
  })
}

watch(landmarks, (newLandmarks) => {
  if (newLandmarks.length > 0 && isDetecting.value) {
    updateDetectedKeypoints(newLandmarks)
    drawUserSkeleton(newLandmarks)
  }
})

const updateDetectedKeypoints = (lm: any[]) => {
  const angles = calculateKeypointAngles(lm)
  const detected: any[] = []
  
  const mapping: Record<string, string> = {
    left_shoulder: 'shoulder', right_shoulder: 'shoulder',
    left_elbow: 'elbow', right_elbow: 'elbow',
    left_hip: 'hip', right_hip: 'hip',
    left_knee: 'knee', right_knee: 'knee'
  }

  for (const [key, angle] of Object.entries(angles)) {
    const displayType = mapping[key]
    if (displayType && typeof angle === 'number') {
      const ann = annotations.value.find((a: any) => a.keypointType === displayType)
      const standardAngle = ann?.standardAngle || 90
      const tolerance = ann?.angleTolerance || 15
      const deviation = angle - standardAngle
      
      let color = '#67c23a'
      if (Math.abs(deviation) > tolerance) color = '#f56c6c'
      else if (Math.abs(deviation) > tolerance * 0.6) color = '#e6a23c'
      
      detected.push({
        type: displayType,
        currentAngle: Math.round(angle),
        deviation: Math.round(deviation),
        color,
        side: key.startsWith('left') ? '左' : '右'
      })
    }
  }
  
  detectedKeypoints.value = detected
}

const drawUserSkeleton = (lm: any[]) => {
  if (!userCanvasRef.value || !userVideoRef.value) return
  const canvas = userCanvasRef.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  const w = canvas.width
  const h = canvas.height
  ctx.clearRect(0, 0, w, h)
  
  const connections = [
    [POSE_LANDMARKS.LEFT_SHOULDER, POSE_LANDMARKS.RIGHT_SHOULDER],
    [POSE_LANDMARKS.LEFT_SHOULDER, POSE_LANDMARKS.LEFT_ELBOW],
    [POSE_LANDMARKS.LEFT_ELBOW, POSE_LANDMARKS.LEFT_WRIST],
    [POSE_LANDMARKS.RIGHT_SHOULDER, POSE_LANDMARKS.RIGHT_ELBOW],
    [POSE_LANDMARKS.RIGHT_ELBOW, POSE_LANDMARKS.RIGHT_WRIST],
    [POSE_LANDMARKS.LEFT_SHOULDER, POSE_LANDMARKS.LEFT_HIP],
    [POSE_LANDMARKS.RIGHT_SHOULDER, POSE_LANDMARKS.RIGHT_HIP],
    [POSE_LANDMARKS.LEFT_HIP, POSE_LANDMARKS.RIGHT_HIP],
    [POSE_LANDMARKS.LEFT_HIP, POSE_LANDMARKS.LEFT_KNEE],
    [POSE_LANDMARKS.LEFT_KNEE, POSE_LANDMARKS.LEFT_ANKLE],
    [POSE_LANDMARKS.RIGHT_HIP, POSE_LANDMARKS.RIGHT_KNEE],
    [POSE_LANDMARKS.RIGHT_KNEE, POSE_LANDMARKS.RIGHT_ANKLE]
  ]
  
  ctx.strokeStyle = '#67c23a'
  ctx.lineWidth = 3
  connections.forEach(([i, j]) => {
    if (lm[i] && lm[j]) {
      ctx.beginPath()
      ctx.moveTo(lm[i].x * w, lm[i].y * h)
      ctx.lineTo(lm[j].x * w, lm[j].y * h)
      ctx.stroke()
    }
  })
  
  const keypointIndices = [
    POSE_LANDMARKS.LEFT_SHOULDER, POSE_LANDMARKS.RIGHT_SHOULDER,
    POSE_LANDMARKS.LEFT_ELBOW, POSE_LANDMARKS.RIGHT_ELBOW,
    POSE_LANDMARKS.LEFT_HIP, POSE_LANDMARKS.RIGHT_HIP,
    POSE_LANDMARKS.LEFT_KNEE, POSE_LANDMARKS.RIGHT_KNEE
  ]
  
  keypointIndices.forEach(idx => {
    if (lm[idx]) {
      const x = lm[idx].x * w
      const y = lm[idx].y * h
      ctx.beginPath()
      ctx.arc(x, y, 8, 0, Math.PI * 2)
      ctx.fillStyle = lm[idx].visibility > 0.5 ? '#67c23a' : '#e6a23c'
      ctx.fill()
      ctx.strokeStyle = '#fff'
      ctx.lineWidth = 2
      ctx.stroke()
    }
  })
}

const startPoseDetection = async () => {
  if (!userVideoRef.value) {
    ElMessage.warning('请先上传视频')
    return
  }
  try {
    isDetecting.value = true
    await initialize(userVideoRef.value)
    ElMessage.success('AI 姿态检测已启动')
  } catch (error) {
    ElMessage.error('启动检测失败')
    isDetecting.value = false
  }
}

const onActionChange = async (actionId: number) => {
  selectedActionId.value = actionId
  loadStdVideo(actionId)
  await loadAnnotations(actionId)
  loadCompareHistory()
  nextTick(() => resizeCanvas())
}

const loadStdVideo = (actionId: number) => {
  stdVideoUrl.value = `/api/videos/standard/${actionId}.mp4`
}

const loadCompareHistory = async () => {
  historyLoading.value = true
  try {
    const response = await axios.get('/api/video-compare/compare/history', {
      params: { actionId: selectedActionId.value },
      headers: { 'X-User-Id': String(userId.value) }
    })
    if (response.data.code === 0) {
      compareHistory.value = response.data.data || []
    }
  } catch {
    compareHistory.value = []
  } finally {
    historyLoading.value = false
  }
}

const onStdVideoLoaded = () => {
  if (stdVideoRef.value) {
    duration.value = stdVideoRef.value.duration
    nextTick(() => resizeCanvas())
  }
}

const onUserVideoLoaded = () => {
  nextTick(() => resizeCanvas())
}

const resizeCanvas = () => {
  if (stdCanvasRef.value && stdVideoRef.value && stdVideoRef.value.clientWidth > 0) {
    stdCanvasRef.value.width = stdVideoRef.value.clientWidth
    stdCanvasRef.value.height = stdVideoRef.value.clientHeight
    drawAnnotations()
  }
  if (userCanvasRef.value && userVideoRef.value && userVideoRef.value.clientWidth > 0) {
    userCanvasRef.value.width = userVideoRef.value.clientWidth
    userCanvasRef.value.height = userVideoRef.value.clientHeight
  }
}

const drawAnnotations = () => {
  if (!stdCanvasRef.value || !stdVideoRef.value || !showAnnotations.value) return
  const canvas = stdCanvasRef.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  const w = canvas.width
  const h = canvas.height
  ctx.clearRect(0, 0, w, h)
  
  annotations.value.forEach((ann: any) => {
    const x = ann.xRatio * w
    const y = ann.yRatio * h
    const timePhase = duration.value > 0 ? (currentTime.value / duration.value) % 1 : 0
    const simulatedAngle = ann.standardAngle + Math.sin(timePhase * Math.PI * 4) * ann.angleTolerance * 0.5
    const deviation = Math.abs(simulatedAngle - ann.standardAngle)
    let colorHex = '#67c23a'
    if (deviation > ann.angleTolerance) colorHex = '#f56c6c'
    else if (deviation > ann.angleTolerance * 0.6) colorHex = '#e6a23c'
    
    ctx.beginPath()
    ctx.arc(x, y, 8, 0, Math.PI * 2)
    ctx.fillStyle = colorHex
    ctx.fill()
    ctx.strokeStyle = '#fff'
    ctx.lineWidth = 2
    ctx.stroke()
    
    if (ann.showArc) {
      const radius = 30
      const startAngle = ((simulatedAngle - 90) * Math.PI) / 180
      const endAngle = startAngle + (simulatedAngle * Math.PI / 180)
      ctx.beginPath()
      ctx.moveTo(x, y)
      ctx.arc(x, y, radius, startAngle, endAngle, false)
      ctx.closePath()
      ctx.fillStyle = colorHex + '66'
      ctx.fill()
      ctx.fillStyle = colorHex
      ctx.font = 'bold 14px Arial'
      ctx.fillText(Math.round(simulatedAngle) + '°', x + radius + 5, y)
    }
    
    ctx.fillStyle = '#fff'
    ctx.font = 'bold 12px Arial'
    ctx.fillText(getKeypointLabel(ann.keypointType), x + 12, y + 4)
  })
}

const onTimeUpdate = () => {
  if (stdVideoRef.value) {
    currentTime.value = stdVideoRef.value.currentTime
    seekPosition.value = duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0
    updateDisplayKeypoints()
    drawAnnotations()
  }
}

const syncPlay = (e: Event) => {
  const target = e.target as HTMLVideoElement
  const other = target === stdVideoRef.value ? userVideoRef.value : stdVideoRef.value
  if (other && other.paused) other.play().catch(() => {})
}

const syncPause = (e: Event) => {
  const target = e.target as HTMLVideoElement
  const other = target === stdVideoRef.value ? userVideoRef.value : stdVideoRef.value
  if (other && !other.paused) other.pause()
}

const playBoth = () => {
  stdVideoRef.value?.play()
  userVideoRef.value?.play()
}

const pauseBoth = () => {
  stdVideoRef.value?.pause()
  userVideoRef.value?.pause()
}

const resetBoth = () => {
  if (stdVideoRef.value) stdVideoRef.value.currentTime = 0
  if (userVideoRef.value) userVideoRef.value.currentTime = 0
  currentTime.value = 0
  seekPosition.value = 0
}

const setSpeed = () => {
  ;[stdVideoRef.value, userVideoRef.value].forEach(v => { if (v) v.playbackRate = playbackSpeed.value })
}

const toggleAnnotations = () => {
  showAnnotations.value = !showAnnotations.value
  if (showAnnotations.value) nextTick(() => drawAnnotations())
  else {
    const ctx = stdCanvasRef.value?.getContext('2d')
    if (ctx && stdCanvasRef.value) ctx.clearRect(0, 0, stdCanvasRef.value.width, stdCanvasRef.value.height)
  }
}

const formatTime = (seconds: number) => {
  if (!seconds || isNaN(seconds)) return '0:00'
  return `${Math.floor(seconds / 60)}:${Math.floor(seconds % 60).toString().padStart(2, '0')}`
}

const formatSeekTooltip = (val: number) => formatTime((val / 100) * duration.value)

const onSeek = (val: number) => {
  const time = (val / 100) * duration.value
  if (stdVideoRef.value) stdVideoRef.value.currentTime = time
  if (userVideoRef.value) userVideoRef.value.currentTime = time
}

const beforeUpload = (file: File) => {
  if (!file.type.startsWith('video/')) {
    ElMessage.error('只能上传视频文件!')
    return false
  }
  if (file.size / 1024 / 1024 > 500) {
    ElMessage.error('视频大小不能超过 500MB!')
    return false
  }
  uploading.value = true
  return true
}

const submitUpload = () => { uploadRef.value?.submit() }

const onUploadProgress = (event: any) => {
  uploadProgress.value = Math.round(event.percent || 0)
}

const onUploadSuccess = (response: any) => {
  uploading.value = false
  uploadProgress.value = 0
  if (response.code === 0) {
    userVideoUrl.value = response.data.videoUrl
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const onUploadError = () => {
  uploading.value = false
  uploadProgress.value = 0
  ElMessage.error('上传失败，请重试')
}

const getScoreType = (score: number) => {
  if (!score || score < 60) return 'danger'
  if (score < 80) return 'warning'
  return 'success'
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = { PENDING: 'info', COMPLETED: 'success', FAILED: 'danger' }
  return types[status] || 'info'
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  loadTrainActions()
  resizeObserver = new ResizeObserver(() => resizeCanvas())
  if (stdPanelRef.value) resizeObserver.observe(stdPanelRef.value)
})

onUnmounted(() => {
  destroy()
  resizeObserver?.disconnect()
})
</script>

<style scoped>
.video-compare-container { padding: 20px; max-width: 1400px; margin: 0 auto; }
.action-select { display: flex; align-items: center; margin-bottom: 20px; }
.muscle-tag { margin-left: 10px; color: #909399; font-size: 12px; }
.action-name { font-size: 14px; color: #67c23a; font-weight: normal; }
.video-container { display: flex; gap: 20px; margin-bottom: 20px; }
.video-panel { flex: 1; background: #1a1a2e; border-radius: 8px; padding: 15px; }
.video-panel h4 { margin: 0 0 10px 0; color: #fff; }
.video-wrapper { position: relative; width: 100%; background: #000; border-radius: 4px; overflow: hidden; }
.video-wrapper video { width: 100%; display: block; }
.annotation-canvas { position: absolute; top: 0; left: 0; pointer-events: none; width: 100%; height: 100%; }
.annotation-legend { display: flex; gap: 15px; margin-top: 10px; padding: 8px; background: rgba(0,0,0,0.3); border-radius: 4px; }
.legend-item { font-size: 12px; }
.legend-item.correct { color: #67c23a; }
.legend-item.warning { color: #e6a23c; }
.legend-item.error { color: #f56c6c; }
.keypoint-info { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 10px; padding: 8px; background: rgba(0,0,0,0.3); border-radius: 4px; }
.kp-item { display: flex; align-items: center; gap: 5px; font-size: 12px; }
.kp-angle { font-size: 14px; }
.kp-range { color: #909399; font-size: 11px; }
.playback-controls { display: flex; gap: 10px; align-items: center; margin-bottom: 15px; padding: 10px; background: #f5f7fa; border-radius: 8px; }
.speed-control { margin-left: 20px; display: flex; align-items: center; gap: 5px; }
.time-display { margin-left: auto; font-family: monospace; font-size: 14px; color: #606266; }
.progress-bar { padding: 0 20px; margin-bottom: 20px; }
.upload-section { margin-top: 30px; padding: 20px; background: #f5f7fa; border-radius: 8px; }
.history-section { margin-top: 30px; }
.detecting-badge { font-size: 12px; color: #67c23a; margin-left: 10px; }
</style>
