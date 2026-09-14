import request from './request'

// ================= 1. 用户认证模块 =================
export const login = (data) => {
  return request.post('/user/login', null, {
    params: data
  })
}

export const register = (data) => {
  const { adminKey, ...restData } = data
  return request.post('/user/register', restData, {
    params: {
      inputKey: adminKey || ''
    }
  })
}

export const registerUser = (data) => {
  return register(data)
}

// ================= 2. 物品管理模块 =================
export const addItem = (data) => {
  return request.post('/item/add', data)
}

export const getList = () => {
  return request.get('/item/list')
}

export const getPending = () => {
  return request.get('/item/pending')
}

export const review = (id, status) => {
  return request.post('/item/review', null, {
    params: { id, status }
  })
}

export const getStats = () => {
  return request.get('/item/stats')
}

export const getMyItems = (userId) => {
  return request.get('/item/my', {
    params: { userId }
  })
}

export const deleteItem = (id, userId) => {
  return request.delete(`/item/delete/${id}`, {
    params: { userId }
  })
}

// ================= 3. 认领流转模块 =================
export const claim = (data) => {
  return request.post('/claim/add', data)
}

export const getClaimList = () => {
  return request.get('/claim/list')
}

export const reviewClaim = (id, status) => {
  return request.post('/claim/review', null, {
    params: { id, status }
  })
}

export const getMyClaims = (userId) => {
  return request.get('/claim/my', {
    params: { userId }
  })
}

// 🆕 拾获者确认认领申请
export const confirmClaim = (claimId, action, userId) => {
  return request.post('/claim/confirm', null, {
    params: { claimId, action, userId }
  })
}

// 🆕 获取待我确认的认领申请
export const getPendingConfirmations = (userId) => {
  return request.get('/claim/pending-confirm', {
    params: { userId }
  })
}

// 🆕 确认归还（自动归档）
export const completeClaim = (claimId, userId) => {
  return request.post('/claim/complete', null, {
    params: { claimId, userId }
  })
}

// 🆕 查看联系方式
export const getContactInfo = (claimId, userId) => {
  return request.get(`/claim/contact/${claimId}`, {
    params: { userId }
  })
}

// ================= 4. 智能匹配模块 =================
export const smartMatch = (item) => {
  return request.post('/item/match', item)
}


