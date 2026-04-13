import { ref, onUnmounted } from 'vue'

// MediaPipe Pose keypoint indices
export const POSE_LANDMARKS = {
  NOSE: 0,
  LEFT_EYE: 1,
  RIGHT_EYE: 2,
  LEFT_EAR: 3,
  RIGHT_EAR: 4,
  LEFT_SHOULDER: 11,
  RIGHT_SHOULDER: 12,
  LEFT_ELBOW: 13,
  RIGHT_ELBOW: 14,
  LEFT_WRIST: 15,
  RIGHT_WRIST: 16,
  LEFT_HIP: 23,
  RIGHT_HIP: 24,
  LEFT_KNEE: 25,
  RIGHT_KNEE: 26,
  LEFT_ANKLE: 27,
  RIGHT_ANKLE: 28
}

export interface PoseKeypoint {
  type: string
  x: number
  y: number
  z: number
  visibility: number
  angle?: number
}

export function useMediaPipePose() {
  const pose = ref<any>(null)
  const camera = ref<any>(null)
  const isLoading = ref(false)
  const isInitialized = ref(false)
  const error = ref<string | null>(null)
  const landmarks = ref<PoseKeypoint[]>([])

  const calculateAngle = (
    point1: { x: number; y: number },
    point2: { x: number; y: number },
    point3: { x: number; y: number }
  ): number => {
    const radians =
      Math.atan2(point3.y - point2.y, point3.x - point2.x) -
      Math.atan2(point1.y - point2.y, point1.x - point2.x)
    let angle = Math.abs((radians * 180) / Math.PI)
    if (angle > 180) {
      angle = 360 - angle
    }
    return angle
  }

  const calculateKeypointAngles = (lm: any[]): Record<string, number> => {
    const angles: Record<string, number> = {}

    if (lm[POSE_LANDMARKS.LEFT_ELBOW] && lm[POSE_LANDMARKS.LEFT_SHOULDER] && lm[POSE_LANDMARKS.LEFT_HIP]) {
      angles.left_shoulder = calculateAngle(
        lm[POSE_LANDMARKS.LEFT_ELBOW],
        lm[POSE_LANDMARKS.LEFT_SHOULDER],
        lm[POSE_LANDMARKS.LEFT_HIP]
      )
    }
    if (lm[POSE_LANDMARKS.RIGHT_ELBOW] && lm[POSE_LANDMARKS.RIGHT_SHOULDER] && lm[POSE_LANDMARKS.RIGHT_HIP]) {
      angles.right_shoulder = calculateAngle(
        lm[POSE_LANDMARKS.RIGHT_ELBOW],
        lm[POSE_LANDMARKS.RIGHT_SHOULDER],
        lm[POSE_LANDMARKS.RIGHT_HIP]
      )
    }

    if (lm[POSE_LANDMARKS.LEFT_SHOULDER] && lm[POSE_LANDMARKS.LEFT_ELBOW] && lm[POSE_LANDMARKS.LEFT_WRIST]) {
      angles.left_elbow = calculateAngle(
        lm[POSE_LANDMARKS.LEFT_SHOULDER],
        lm[POSE_LANDMARKS.LEFT_ELBOW],
        lm[POSE_LANDMARKS.LEFT_WRIST]
      )
    }
    if (lm[POSE_LANDMARKS.RIGHT_SHOULDER] && lm[POSE_LANDMARKS.RIGHT_ELBOW] && lm[POSE_LANDMARKS.RIGHT_WRIST]) {
      angles.right_elbow = calculateAngle(
        lm[POSE_LANDMARKS.RIGHT_SHOULDER],
        lm[POSE_LANDMARKS.RIGHT_ELBOW],
        lm[POSE_LANDMARKS.RIGHT_WRIST]
      )
    }

    if (lm[POSE_LANDMARKS.LEFT_SHOULDER] && lm[POSE_LANDMARKS.LEFT_HIP] && lm[POSE_LANDMARKS.LEFT_KNEE]) {
      angles.left_hip = calculateAngle(
        lm[POSE_LANDMARKS.LEFT_SHOULDER],
        lm[POSE_LANDMARKS.LEFT_HIP],
        lm[POSE_LANDMARKS.LEFT_KNEE]
      )
    }
    if (lm[POSE_LANDMARKS.RIGHT_SHOULDER] && lm[POSE_LANDMARKS.RIGHT_HIP] && lm[POSE_LANDMARKS.RIGHT_KNEE]) {
      angles.right_hip = calculateAngle(
        lm[POSE_LANDMARKS.RIGHT_SHOULDER],
        lm[POSE_LANDMARKS.RIGHT_HIP],
        lm[POSE_LANDMARKS.RIGHT_KNEE]
      )
    }

    if (lm[POSE_LANDMARKS.LEFT_HIP] && lm[POSE_LANDMARKS.LEFT_KNEE] && lm[POSE_LANDMARKS.LEFT_ANKLE]) {
      angles.left_knee = calculateAngle(
        lm[POSE_LANDMARKS.LEFT_HIP],
        lm[POSE_LANDMARKS.LEFT_KNEE],
        lm[POSE_LANDMARKS.LEFT_ANKLE]
      )
    }
    if (lm[POSE_LANDMARKS.RIGHT_HIP] && lm[POSE_LANDMARKS.RIGHT_KNEE] && lm[POSE_LANDMARKS.RIGHT_ANKLE]) {
      angles.right_knee = calculateAngle(
        lm[POSE_LANDMARKS.RIGHT_HIP],
        lm[POSE_LANDMARKS.RIGHT_KNEE],
        lm[POSE_LANDMARKS.RIGHT_ANKLE]
      )
    }

    return angles
  }

  const initialize = async (videoElement: HTMLVideoElement) => {
    isLoading.value = true
    error.value = null

    try {
      const { Pose, Camera } = await import('@mediapipe/pose')

      pose.value = new Pose({
        locateFile: (file: string) => {
          return `https://cdn.jsdelivr.net/npm/@mediapipe/pose/${file}`
        }
      })

      pose.value.setOptions({
        modelComplexity: 1,
        smoothLandmarks: true,
        enableSegmentation: false,
        minDetectionConfidence: 0.5,
        minTrackingConfidence: 0.5
      })

      pose.value.onResults((results: any) => {
        if (results.poseLandmarks) {
          landmarks.value = results.poseLandmarks.map((lm: any, idx: number) => ({
            type: getKeypointName(idx),
            x: lm.x,
            y: lm.y,
            z: lm.z,
            visibility: lm.visibility,
            angle: 0
          }))
        } else {
          landmarks.value = []
        }
      })

      camera.value = new Camera(videoElement, {
        onFrame: async () => {
          if (pose.value) {
            await pose.value.send({ image: videoElement })
          }
        },
        width: 640,
        height: 480
      })

      await camera.value.start()
      isInitialized.value = true
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to initialize MediaPipe Pose'
      console.error('MediaPipe Pose init error:', e)
    } finally {
      isLoading.value = false
    }
  }

  const getKeypointName = (index: number): string => {
    const names: Record<number, string> = {
      [POSE_LANDMARKS.NOSE]: 'nose',
      [POSE_LANDMARKS.LEFT_SHOULDER]: 'left_shoulder',
      [POSE_LANDMARKS.RIGHT_SHOULDER]: 'right_shoulder',
      [POSE_LANDMARKS.LEFT_ELBOW]: 'left_elbow',
      [POSE_LANDMARKS.RIGHT_ELBOW]: 'right_elbow',
      [POSE_LANDMARKS.LEFT_WRIST]: 'left_wrist',
      [POSE_LANDMARKS.RIGHT_WRIST]: 'right_wrist',
      [POSE_LANDMARKS.LEFT_HIP]: 'left_hip',
      [POSE_LANDMARKS.RIGHT_HIP]: 'right_hip',
      [POSE_LANDMARKS.LEFT_KNEE]: 'left_knee',
      [POSE_LANDMARKS.RIGHT_KNEE]: 'right_knee',
      [POSE_LANDMARKS.LEFT_ANKLE]: 'left_ankle',
      [POSE_LANDMARKS.RIGHT_ANKLE]: 'right_ankle'
    }
    return names[index] || `point_${index}`
  }

  const destroy = () => {
    if (camera.value) {
      camera.value.stop()
      camera.value = null
    }
    if (pose.value) {
      pose.value.close()
      pose.value = null
    }
    isInitialized.value = false
    landmarks.value = []
  }

  onUnmounted(() => {
    destroy()
  })

  return {
    pose,
    camera,
    landmarks,
    isLoading,
    isInitialized,
    error,
    initialize,
    destroy,
    calculateAngle,
    calculateKeypointAngles
  }
}
