<template>
  <div class="video-compare-container">
    <h2>健身动作视频对比</h2>
    
    <!-- 动作选择 -->
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
      <el-button type="primary" @click="startCompare" :disabled="!userVideoUrl" style="margin-left: 10px">
        开始对比
      </el-button>
    </div>

    <!-- 双屏视频播放器 -->
    <div class="video-container" v-if="selectedActionId">
      <!-- 标准动作视频 -->
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
          <!-- 关键部位标注Canvas -->
          <canvas ref="stdCanvasRef" class="annotation-canvas"></canvas>
        </div>
        <div class="annotation-legend">
          <span class="legend-item correct">● 绿色 = 正确</span>
          <span class="legend-item warning">● 黄色 = 临界</span>
          <span class="legend-item error">● 红色 = 错误</span>
        </div>
        <!-- 关键部位角度信息 -->
        <div class="keypoint-info" v-if="showAnnotations">
          <div v-for="kp in displayKeypoints" :key="kp.type" class="kp-item" :style="{ color: kp.color }">
            <span class="kp-type">{{ getKeypointLabel(kp.type) }}</span>
            <span class="kp-angle">{{ kp.currentAngle }}°</span>
            <span class="kp-range">({{ kp.standardAngle - kp.tolerance }}°~{{ kp.standardAngle + kp.tolerance }}°)</span>
          </div>
        </div>
      </div>
      
      <!-- 用户动作视频 -->
      <div class="video-panel" ref="userPanelRef">
        <h4>我的动作</h4>
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
        </div>
      </div>
    </div>

    <!-- 播放控制 -->
    <div class="playback-controls" v-if="selectedActionId">
      <el-button @click="playBoth" type="primary">▶ 播放</el-button>
      <el-button @click="pauseBoth">⏸ 暂停</el-button>
      <el-button @click="resetBoth">⏮ 重置</el-button>
      <el-button @click="toggleAnnotations">{{ showAnnotations ? '隐藏标注' : '显示标注' }}</el-button>
      <span class="speed-control">
        倍速:
        <el-select v-model="playbackSpeed" @change="setSpeed" size="small">
          <el-option label="0.25x" :value="0.25"></el-option>
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

    <!-- 进度条 -->
    <div class="progress-bar" v-if="selectedActionId && duration > 0">
      <el-slider 
        v-model="seekPosition" 
        :min="0" 
        :max="100" 
        :format-tooltip="formatSeekTooltip"
        @change="onSeek"
      />
    </div>

    <!-- 视频上传 -->
    <div class="upload-section">
      <h3>上传我的动作视频</h3>
      <el-upload
        ref="uploadRef"
        action="/api/video-compare/upload"
        :headers="uploadHeaders"
        :data="uploadData"
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
      <!-- 上传进度 -->
      <el-progress 
        v-if="uploadProgress > 0" 
        :percentage="uploadProgress" 
        :status="uploadProgress === 100 ? 'success' : ''"
        style="margin-top: 10px"
      />
    </div>

    <!-- 历史对比记录 -->
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
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">查看详情</el-button>
            <el-button size="small" type="danger" @click="deleteRecord(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="对比详情" width="800px">
      <div v-if="currentRecord" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录ID">{{ currentRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="综合评分">
            <el-tag :type="getScoreType(currentRecord.overallScore)" size="large">
              {{ currentRecord.overallScore || '待评分' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentRecord.status }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRecord.createdTime }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="compareResult" class="compare-result-details">
          <h4>详细分析</h4>
          <div v-for="(item, index) in compareResult.keypoints || []" :key="index" class="kp-detail">
            <span class="kp-name">{{ getKeypointLabel(item.type) }}</span>
            <span>检测角度: {{ item.detectedAngle }}°</span>
            <span>标准角度: {{ item.standardAngle }}°</span>
            <span>偏差: {{ item.deviation }}°</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const userId = ref(1)
const selectedActionId = ref(null)
const trainActions = ref([])
const stdVideoUrl = ref('')
const userVideoUrl = ref('')
const compareHistory = ref([])
const playbackSpeed = ref(1)
const currentTime = ref(0)
const duration = ref(0)
const seekPosition = ref(0)
const showAnnotations = ref(true)
const uploadProgress = ref(0)
const uploading = ref(false)
const historyLoading = ref(false)
const detailDialogVisible = ref(false)
const currentRecord = ref(null)
const compareResult = ref(null)
const annotations = ref([])
const displayKeypoints = ref([])

const stdVideoRef = ref(null)
const userVideoRef = ref(null)
const stdCanvasRef = ref(null)
const stdPanelRef = ref(null)
const uploadRef = ref(null)

const uploadHeaders = computed(() => ({
  'X-User-Id': String(userId.value)
}))

const uploadData = computed(() => ({
  actionId: selectedActionId.value,
  category: stdActionName.value
}))

const stdActionName = computed(() => {
  const action = trainActions.value.find(a => a.id === selectedActionId.value)
  return action ? action.name : ''
})

// 关键部位标签映射
const keypointLabels = {
  shoulder: '肩部',
  elbow: '肘部', 
  waist: '腰部',
  knee: '膝部',
  hip: '髋部'
}

const getKeypointLabel = (type) => keypointLabels[type] || type

// 加载训练动作列表
const loadTrainActions = async () => {
  try {
    const response = await axios.get('/api/train/actions')
    if (response.data.code === 0) {
      trainActions.value = response.data.data || []
    }
  } catch (error) {
    trainActions.value = [
      { id: 1, name: '杠铃卧推', muscleGroup: 'chest', videoUrl: '' },
      { id: 2, name: '哑铃上斜卧推', muscleGroup: 'chest', videoUrl: '' },
      { id: 3, name: '宽握引体向上', muscleGroup: 'back', videoUrl: '' },
      { id: 4, name: '杠铃深蹲', muscleGroup: 'leg', videoUrl: '' },
      { id: 5, name: '平板支撑', muscleGroup: 'core', videoUrl: '' }
    ]
  }
}

// 加载关键部位标注配置
const loadAnnotations = async (actionId) => {
  try {
    const response = await axios.get(`/api/video-compare/annotations/${actionId}`)
    if (response.data.code === 0) {
      annotations.value = response.data.data || []
    }
  } catch (error) {
    annotations.value = getDefaultAnnotations(actionId)
  }
  updateDisplayKeypoints()
}

// 获取默认标注配置
const getDefaultAnnotations = (actionId) => {
  const defaults = {
    1: [
      { keypointType: 'shoulder', xRatio: 0.3, yRatio: 0.35, standardAngle: 45, angleTolerance: 15, color: 'green', showArc: 1, showRange: 1, showTrail: 0 },
      { keypointType: 'elbow', xRatio: 0.35, yRatio: 0.45, standardAngle: 90, angleTolerance: 20, color: 'green', showArc: 1, showRange: 1, showTrail: 0 },
      { keypointType: 'hip', xRatio: 0.5, yRatio: 0.6, standardAngle: 180, angleTolerance: 10, color: 'green', showArc: 1, showRange: 1, showTrail: 0 }
    ],
    4: [
      { keypointType: 'knee', xRatio: 0.5, yRatio: 0.55, standardAngle: 90, angleTolerance: 25, color: 'green', showArc: 1, showRange: 1, showTrail: 0 },
      { keypointType: 'hip', xRatio: 0.5, yRatio: 0.5, standardAngle: 90, angleTolerance: 20, color: 'green', showArc: 1, showRange: 1, showTrail: 0 },
      { keypointType: 'waist', xRatio: 0.5, yRatio: 0.4, standardAngle: 180, angleTolerance: 15, color: 'green', showArc: 1, showRange: 1, showTrail: 0 }
    ]
  }
  return defaults[actionId] || [
    { keypointType: 'shoulder', xRatio: 0.3, yRatio: 0.3, standardAngle: 45, angleTolerance: 15, color: 'green', showArc: 1, showRange: 1, showTrail: 0 },
    { keypointType: 'elbow', xRatio: 0.35, yRatio: 0.4, standardAngle: 90, angleTolerance: 20, color: 'green', showArc: 1, showRange: 1, showTrail: 0 },
    { keypointType: 'waist', xRatio: 0.5, yRatio: 0.5, standardAngle: 180, angleTolerance: 10, color: 'green', showArc: 1, showRange: 1, showTrail: 0 },
    { keypointType: 'knee', xRatio: 0.5, yRatio: 0.6, standardAngle: 90, angleTolerance: 25, color: 'green', showArc: 1, showRange: 1, showTrail: 0 },
    { keypointType: 'hip', xRatio: 0.5, yRatio: 0.45, standardAngle: 90, angleTolerance: 20, color: 'green', showArc: 1, showRange: 1, showTrail: 0 }
  ]
}

// 更新显示的关键点数据
const updateDisplayKeypoints = () => {
  displayKeypoints.value = annotations.value.map(ann => {
    const timePhase = duration.value > 0 ? (currentTime.value / duration.value) % 1 : 0
    const simulatedAngle = ann.standardAngle + Math.sin(timePhase * Math.PI * 4) * ann.angleTolerance * 0.5
    const deviation = Math.abs(simulatedAngle - ann.standardAngle)
    let color = '#67c23a'
    if (deviation > ann.angleTolerance) {
      color = '#f56c6c'
    } else if (deviation > ann.angleTolerance * 0.6) {
      color = '#e6a23c'
    }
    return {
      type: ann.keypointType,
      currentAngle: Math.round(simulatedAngle),
      standardAngle: ann.standardAngle,
      tolerance: ann.angleTolerance,
      color
    }
  })
}

// 动作选择变更
const onActionChange = async (actionId) => {
  selectedActionId.value = actionId
  loadStdVideo(actionId)
  await loadAnnotations(actionId)
  loadCompareHistory()
  nextTick(() => resizeCanvas())
}

// 加载标准动作视频
const loadStdVideo = (actionId) => {
  const action = trainActions.value.find(a => a.id === actionId)
  if (action && action.videoUrl) {
    stdVideoUrl.value = action.videoUrl
  } else {
    stdVideoUrl.value = `https://example.com/videos/standard/${actionId}.mp4`
  }
}

// 加载对比历史
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
  } catch (error) {
    compareHistory.value = []
  } finally {
    historyLoading.value = false
  }
}

// 视频元数据加载
const onStdVideoLoaded = () => {
  if (stdVideoRef.value) {
    duration.value = stdVideoRef.value.duration
    nextTick(() => resizeCanvas())
  }
}

const onUserVideoLoaded = () => {
  nextTick(() => resizeCanvas())
}

// 调整Canvas大小
const resizeCanvas = () => {
  if (stdCanvasRef.value && stdVideoRef.value) {
    const video = stdVideoRef.value
    const canvas = stdCanvasRef.value
    if (video.clientWidth > 0 && video.clientHeight > 0) {
      canvas.width = video.clientWidth
      canvas.height = video.clientHeight
      drawAnnotations()
    }
  }
}

// 绘制标注
const drawAnnotations = () => {
  if (!stdCanvasRef.value || !stdVideoRef.value || !showAnnotations.value) return
  
  const canvas = stdCanvasRef.value
  const ctx = canvas.getContext('2d')
  const w = canvas.width
  const h = canvas.height
  
  ctx.clearRect(0, 0, w, h)
  
  annotations.value.forEach(ann => {
    const x = ann.xRatio * w
    const y = ann.yRatio * h
    const timePhase = duration.value > 0 ? (currentTime.value / duration.value) % 1 : 0
    
    const simulatedAngle = ann.standardAngle + Math.sin(timePhase * Math.PI * 4) * ann.angleTolerance * 0.5
    const deviation = Math.abs(simulatedAngle - ann.standardAngle)
    let colorHex = '#67c23a'
    if (deviation > ann.angleTolerance) {
      colorHex = '#f56c6c'
    } else if (deviation > ann.angleTolerance * 0.6) {
      colorHex = '#e6a23c'
    }
    
    // 绘制关键点圆圈
    ctx.beginPath()
    ctx.arc(x, y, 8, 0, Math.PI * 2)
    ctx.fillStyle = colorHex
    ctx.fill()
    ctx.strokeStyle = '#fff'
    ctx.lineWidth = 2
    ctx.stroke()
    
    // 绘制角度弧线
    if (ann.showArc) {
      drawAngleArc(ctx, x, y, simulatedAngle, ann.standardAngle, ann.angleTolerance, colorHex)
    }
    
    // 绘制幅度范围框
    if (ann.showRange) {
      drawRangeBox(ctx, x, y, ann.standardAngle, ann.angleTolerance, colorHex)
    }
    
    // 绘制轨迹引导线
    if (ann.showTrail) {
      drawTrajectoryLine(ctx, x, y, ann, timePhase)
    }
    
    // 绘制标签
    ctx.fillStyle = '#fff'
    ctx.font = 'bold 12px Arial'
    ctx.fillText(getKeypointLabel(ann.keypointType), x + 12, y + 4)
  })
}

// 绘制角度弧线
const drawAngleArc = (ctx, x, y, currentAngle, standardAngle, tolerance, color) => {
  const radius = 30
  const startAngle = ((currentAngle - 90) * Math.PI) / 180
  const endAngle = startAngle + (currentAngle * Math.PI / 180)
  
  ctx.beginPath()
  ctx.moveTo(x, y)
  ctx.arc(x, y, radius, startAngle, endAngle, false)
  ctx.closePath()
  ctx.fillStyle = color + '66'
  ctx.fill()
  
  ctx.fillStyle = color
  ctx.font = 'bold 14px Arial'
  ctx.fillText(Math.round(currentAngle) + '°', x + radius + 5, y)
}

// 绘制幅度范围框
const drawRangeBox = (ctx, x, y, standardAngle, tolerance, color) => {
  const boxWidth = 60
  const boxHeight = 30
  const minAngle = standardAngle - tolerance
  const maxAngle = standardAngle + tolerance
  
  ctx.strokeStyle = color
  ctx.lineWidth = 1
  ctx.setLineDash([5, 5])
  ctx.strokeRect(x - boxWidth/2, y - boxHeight/2, boxWidth, boxHeight)
  ctx.setLineDash([])
  
  ctx.fillStyle = 'rgba(0,0,0,0.5)'
  ctx.fillRect(x - boxWidth/2, y - boxHeight/2, boxWidth, boxHeight)
  ctx.strokeRect(x - boxWidth/2, y - boxHeight/2, boxWidth, boxHeight)
  
  ctx.fillStyle = '#fff'
  ctx.font = '10px Arial'
  ctx.textAlign = 'center'
  ctx.fillText(`${minAngle}°~${maxAngle}°`, x, y + 4)
  ctx.textAlign = 'start'
}

// 绘制轨迹引导线
const drawTrajectoryLine = (ctx, x, y, ann, timePhase) => {
  ctx.beginPath()
  ctx.setLineDash([3, 3])
  
  const amplitude = 20
  const startX = x - 50
  const endX = x + 50
  
  for (let px = startX; px <= endX; px += 2) {
    const progress = (px - startX) / (endX - startX)
    const expectedY = y + Math.sin(progress * Math.PI) * amplitude
    if (px === startX) {
      ctx.moveTo(px, expectedY)
    } else {
      ctx.lineTo(px, expectedY)
    }
  }
  
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.5)'
  ctx.lineWidth = 2
  ctx.stroke()
  ctx.setLineDash([])
}

// 时间更新
const onTimeUpdate = () => {
  if (stdVideoRef.value) {
    currentTime.value = stdVideoRef.value.currentTime
    seekPosition.value = duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0
    updateDisplayKeypoints()
    drawAnnotations()
  }
}

// 同步播放
const syncPlay = (e) => {
  const target = e.target
  const other = target === stdVideoRef.value ? userVideoRef.value : stdVideoRef.value
  if (other && other.paused) {
    other.play().catch(() => {})
  }
}

const syncPause = (e) => {
  const target = e.target
  const other = target === stdVideoRef.value ? userVideoRef.value : stdVideoRef.value
  if (other && !other.paused) {
    other.pause()
  }
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
  const videos = [stdVideoRef.value, userVideoRef.value]
  videos.forEach(v => { if (v) v.playbackRate = playbackSpeed.value })
}

const toggleAnnotations = () => {
  showAnnotations.value = !showAnnotations.value
  if (showAnnotations.value) {
    nextTick(() => drawAnnotations())
  } else {
    const canvas = stdCanvasRef.value
    if (canvas) {
      const ctx = canvas.getContext('2d')
      ctx.clearRect(0, 0, canvas.width, canvas.height)
    }
  }
}

const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '0:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const formatSeekTooltip = (val) => {
  const time = (val / 100) * duration.value
  return formatTime(time)
}

const onSeek = (val) => {
  const time = (val / 100) * duration.value
  if (stdVideoRef.value) stdVideoRef.value.currentTime = time
  if (userVideoRef.value) userVideoRef.value.currentTime = time
}

// 上传相关
const beforeUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isLt500M = file.size / 1024 / 1024 < 500
  
  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return false
  }
  if (!isLt500M) {
    ElMessage.error('视频大小不能超过 500MB!')
    return false
  }
  uploading.value = true
  return true
}

const submitUpload = () => {
  uploadRef.value?.submit()
}

const onUploadProgress = (event) => {
  uploadProgress.value = Math.round(event.percent || 0)
}

const onUploadSuccess = (response) => {
  uploading.value = false
  uploadProgress.value = 0
  if (response.code === 0) {
    userVideoUrl.value = response.data.videoUrl
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败: ' + (response.msg || '未知错误'))
  }
}

const onUploadError = () => {
  uploading.value = false
  uploadProgress.value = 0
  ElMessage.error('上传失败，请重试')
}

// 开始对比
const startCompare = async () => {
  if (!userVideoUrl.value) {
    ElMessage.warning('请先上传您的动作视频')
    return
  }
  try {
    const response = await axios.post('/api/video-compare/compare', {
      userVideoId: null,
      stdActionId: selectedActionId.value
    }, {
      headers: { 'X-User-Id': String(userId.value) }
    })
    if (response.data.code === 0) {
      ElMessage.success('对比任务已创建')
      loadCompareHistory()
    }
  } catch (error) {
    ElMessage.error('创建对比失败')
  }
}

// 查看详情
const viewDetail = async (row) => {
  currentRecord.value = row
  detailDialogVisible.value = true
  try {
    const response = await axios.get(`/api/video-compare/compare/${row.id}`)
    if (response.data.code === 0) {
      compareResult.value = response.data.data
    }
  } catch (error) {
    compareResult.value = null
  }
}

// 删除记录
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条对比记录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`/api/video-compare/compare/${row.id}`, {
      headers: { 'X-User-Id': String(userId.value) }
    })
    ElMessage.success('删除成功')
    loadCompareHistory()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 评分标签类型
const getScoreType = (score) => {
  if (!score || score < 60) return 'danger'
  if (score < 80) return 'warning'
  return 'success'
}

const getStatusType = (status) => {
  const types = {
    PENDING: 'info',
    COMPLETED: 'success',
    FAILED: 'danger'
  }
  return types[status] || 'info'
}

// 监听窗口大小变化
let resizeObserver = null

onMounted(() => {
  loadTrainActions()
  
  resizeObserver = new ResizeObserver(() => {
    resizeCanvas()
  })
  
  if (stdPanelRef.value) {
    resizeObserver.observe(stdPanelRef.value)
  }
  
  watch(playbackSpeed, () => setSpeed())
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})
</script>

<style scoped>
.video-compare-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}
.action-select {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.muscle-tag {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
.action-name {
  font-size: 14px;
  color: #67c23a;
  font-weight: normal;
}
.video-container {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.video-panel {
  flex: 1;
  background: #1a1a2e;
  border-radius: 8px;
  padding: 15px;
}
.video-panel h4 {
  margin: 0 0 10px 0;
  color: #fff;
}
.video-wrapper {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 4px;
  overflow: hidden;
}
.video-wrapper video {
  width: 100%;
  display: block;
}
.annotation-canvas {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  width: 100%;
  height: 100%;
}
.annotation-legend {
  display: flex;
  gap: 15px;
  margin-top: 10px;
  padding: 8px;
  background: rgba(0,0,0,0.3);
  border-radius: 4px;
}
.legend-item {
  font-size: 12px;
}
.legend-item.correct { color: #67c23a; }
.legend-item.warning { color: #e6a23c; }
.legend-item.error { color: #f56c6c; }
.keypoint-info {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
  padding: 8px;
  background: rgba(0,0,0,0.3);
  border-radius: 4px;
}
.kp-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}
.kp-type {
  font-weight: bold;
}
.kp-angle {
  font-size: 14px;
}
.kp-range {
  color: #909399;
  font-size: 11px;
}
.playback-controls {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}
.speed-control {
  margin-left: 20px;
  display: flex;
  align-items: center;
  gap: 5px;
}
.time-display {
  margin-left: auto;
  font-family: monospace;
  font-size: 14px;
  color: #606266;
}
.progress-bar {
  padding: 0 20px;
  margin-bottom: 20px;
}
.upload-section {
  margin-top: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}
.upload-section h3 {
  margin-top: 0;
}
.history-section {
  margin-top: 30px;
}
.history-section h3 {
  margin-top: 0;
}
.detail-content {
  padding: 10px;
}
.compare-result-details {
  margin-top: 20px;
}
.compare-result-details h4 {
  margin-bottom: 10px;
}
.kp-detail {
  display: flex;
  gap: 15px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 5px;
}
.kp-name {
  font-weight: bold;
  min-width: 60px;
}
</style>
