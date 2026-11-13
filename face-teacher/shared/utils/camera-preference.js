const CAMERA_POSITION_KEY = 'preferredCameraPosition'

/**
 * @returns {'front'|'back'|''}
 */
export function getPreferredCameraPosition() {
  const value = uni.getStorageSync(CAMERA_POSITION_KEY)
  return value === 'back' || value === 'front' ? value : ''
}

/**
 * @param {'front'|'back'} position
 */
export function setPreferredCameraPosition(position) {
  if (position !== 'front' && position !== 'back') return
  uni.setStorageSync(CAMERA_POSITION_KEY, position)
}

export function clearPreferredCameraPosition() {
  uni.removeStorageSync(CAMERA_POSITION_KEY)
}
