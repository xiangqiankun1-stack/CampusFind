<template>
  <div class="page-container animate-fade-in">
    <div class="page-header">
      <div class="header-left">
        <h3 class="page-title">⏳ 待我确认的认领申请</h3>
        <p class="page-subtitle">以下是申请认领您发布物品的记录</p>
      </div>
      <div class="header-right">
        <el-tag type="info" effect="plain" class="stats-tag">
          共 {{ list.length }} 条申请
        </el-tag>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="list" border stripe class="custom-table" v-loading="loading">
        <el-table-column label="物品名称" min-width="180">
          <template #default="scope">
            <div class="item-name-cell">
              <el-icon class="item-icon"><Box /></el-icon>
              <span>{{ scope.row.itemName || '未知物品' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="申请人" width="140" align="center">
          <template #default="scope">
            <el-tag size="small" type="info" effect="light" class="user-badge">
              <el-icon><User /></el-icon>
              用户{{ scope.row.userId }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="申请说明" min-width="250" show-overflow-tooltip>
          <template #default="scope">
            <div class="description-cell">
              <el-icon class="desc-icon"><ChatDotRound /></el-icon>
              <span>{{ scope.row.description || '暂无说明' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <!-- 待确认状态 -->
              <template v-if="scope.row.status === 0">
                <el-button 
                  type="success" 
                  size="default"
                  @click="handleConfirm(scope.row, 1)"
                  class="action-btn confirm-btn"
                >
                  <el-icon><CircleCheck /></el-icon>
                  同意联系
                </el-button>
                <el-button 
                  type="danger" 
                  size="default"
                  @click="handleConfirm(scope.row, 2)"
                  class="action-btn reject-btn"
                >
                  <el-icon><CircleClose /></el-icon>
                  拒绝
                </el-button>
              </template>

              <!-- 已同意状态 -->
              <template v-else-if="scope.row.status === 1">
                <el-button 
                  type="primary" 
                  size="default"
                  @click="handleComplete(scope.row)"
                  class="action-btn complete-btn"
                >
                  <el-icon><Box /></el-icon>
                  确认归还
                </el-button>
              </template>

              <!-- 其他状态 -->
              <template v-else>
                <el-tag type="info" size="small" effect="plain">
                  <el-icon><InfoFilled /></el-icon>
                  已完成
                </el-tag>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty 
        v-if="list.length === 0 && !loading" 
        description="暂无待确认的认领申请"
        :image-size="120"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPendingConfirmations, confirmClaim, completeClaim } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box, User, ChatDotRound, CircleCheck, CircleClose, InfoFilled } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const user = JSON.parse(localStorage.getItem('user') || '{}')

const loadData = async () => {
  if (!user.id) {
    ElMessage.error('请先登录')
    return
  }

  loading.value = true
  try {
    const res = await getPendingConfirmations(user.id)
    list.value = res || []
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const handleConfirm = async (claim, action) => {
  try {
    await ElMessageBox.confirm(
        `确定要${action === 1 ? '同意' : '拒绝'}吗？`,
        '提示',
        { confirmButtonText: '确定', cancelButtonText: '取消' }
    )

    const res = await confirmClaim(claim.id, action, user.id)

    if (action === 1) {
      const contact = res.data?.publisherContact || '未设置'
      ElMessage.success(`✅ 已同意！对方联系方式：${contact}`)
      loadData()
    } else {
      ElMessage.info('❌ 已拒绝')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleComplete = async (claim) => {
  try {
    await ElMessageBox.confirm('确认已归还？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })

    await completeClaim(claim.id, user.id)
    ElMessage.success('📦 物品已标记为已归还')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
/* 🎯 页面容器 */
.page-container {
  padding: 16px 24px;
}

.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 📋 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  flex: 1;
}

.page-title {
  margin: 0 0 4px 0;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.stats-tag {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
}

/* 📊 表格卡片 */
.table-card {
  border-radius: 14px !important;
  background-color: #fff;
  border: 1px solid #f1f5f9 !important;
}

.custom-table {
  border-radius: 12px;
  overflow: hidden;
}

.custom-table :deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.custom-table :deep(.el-table__header th) {
  background: transparent !important;
  color: #475569 !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  padding: 16px 0 !important;
  border-bottom: 2px solid #cbd5e1 !important;
}

.custom-table :deep(.el-table__body td) {
  padding: 16px 8px !important;
  vertical-align: middle !important;
}

.custom-table :deep(.el-table__row:hover) {
  background-color: #f8fafc !important;
}

/* 📦 物品名称单元格 */
.item-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-icon {
  font-size: 20px;
  color: #4f46e5;
}

.item-name-cell span {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

/* 👤 用户徽章 */
.user-badge {
  border-radius: 20px;
  padding: 4px 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.user-badge .el-icon {
  font-size: 12px;
}

/* 💬 描述单元格 */
.description-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 4px 0;
}

.desc-icon {
  font-size: 16px;
  color: #4f46e5;
  flex-shrink: 0;
  margin-top: 2px;
}

.description-cell span {
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

/* 🔘 操作按钮区域 */
.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  align-items: center;
  padding: 4px 0;
}

.action-btn {
  min-width: 100px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 13px;
  padding: 8px 16px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.action-btn .el-icon {
  font-size: 14px;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.confirm-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  color: white;
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.reject-btn {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  color: white;
}

.reject-btn:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.complete-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%);
  border: none;
  color: white;
}

.complete-btn:hover {
  background: linear-gradient(135deg, #4338ca 0%, #3730a3 100%);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}
</style>
