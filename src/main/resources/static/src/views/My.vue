<template>
  <div class="my-claims-container animate-fade-in">
    <div class="page-header">
      <div class="header-left">
        <h3 class="page-title">📋 我的认领申请记录</h3>
        <p class="page-subtitle">实时追踪你提交的物品认领单审批进度</p>
      </div>
      <div class="header-right">
        <el-tag type="info" effect="plain" class="stats-tag">
          共 {{ list.length }} 条申请
        </el-tag>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="list" border stripe class="claims-table" v-loading="loading">
        <el-table-column label="申请物品" min-width="180">
          <template #default="scope">
            <div class="item-info-cell">
              <el-icon class="item-icon"><Box /></el-icon>
              <div class="item-details">
                <span class="item-name">{{ scope.row.itemName || '未知物品' }}</span>
                <span class="item-id">ID: {{ scope.row.itemId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="认领凭证说明" min-width="240" show-overflow-tooltip>
          <template #default="scope">
            <div class="description-cell">
              <el-icon class="desc-icon"><ChatDotRound /></el-icon>
              <span>{{ scope.row.description || '暂无说明' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="审核状态" width="150" align="center">
          <template #default="scope">
            <el-tag 
              :type="getStatusType(scope.row.status)" 
              effect="dark"
              class="status-badge"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <!-- 已同意：显示查看联系方式 -->
              <template v-if="scope.row.status === 1">
                <el-button
                    type="primary"
                    size="default"
                    @click="handleViewContact(scope.row)"
                    class="action-btn contact-btn"
                >
                  <el-icon><Phone /></el-icon>
                  查看联系方式
                </el-button>
              </template>

              <!-- 待确认 -->
              <template v-else-if="scope.row.status === 0">
                <el-tag type="warning" size="small" effect="plain">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  等待确认中
                </el-tag>
              </template>

              <!-- 已拒绝 -->
              <template v-else-if="scope.row.status === 2">
                <el-tag type="danger" size="small" effect="plain">
                  <el-icon><CircleClose /></el-icon>
                  已被拒绝
                </el-tag>
              </template>

              <!-- 已完成 -->
              <template v-else-if="scope.row.status === 5">
                <el-tag type="success" size="small" effect="plain">
                  <el-icon><CircleCheck /></el-icon>
                  已完成
                </el-tag>
              </template>

              <!-- 其他状态 -->
              <template v-else>
                <span class="no-action">-</span>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty 
        v-if="list.length === 0 && !loading" 
        description="你当前还没有发起过任何认领申请哦"
        :image-size="120"
      >
        <el-button type="primary" @click="$router.push('/home')">
          去大厅看看
        </el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyClaims, getContactInfo, completeClaim } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box, ChatDotRound, Phone, Loading, CircleClose, CircleCheck } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const user = JSON.parse(localStorage.getItem('user') || '{}')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyClaims(user.id)
    list.value = res || []
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载认领记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const handleViewContact = async (claim) => {
  try {
    const res = await getContactInfo(claim.id, user.id)

    const contact = res.data?.publisherContact || res.publisherContact || '未设置'
    const name = res.data?.publisherName || res.publisherName || '未知'

    await ElMessageBox.alert(
        `<div class="contact-dialog-content">
          <div class="contact-item">
            <span class="contact-label">发布者：</span>
            <span class="contact-value">${name}</span>
          </div>
          <div class="contact-item">
            <span class="contact-label">联系方式：</span>
            <span class="contact-value highlight">${contact}</span>
          </div>
          <div class="contact-tip">
            💡 请礼貌联系对方，核实物品信息后约定归还时间地点
          </div>
        </div>`,
        '对方联系方式',
        {
          confirmButtonText: '知道了',
          type: 'info',
          dangerouslyUseHTMLString: true
        }
    )
  } catch (error) {
    console.error(error)
    ElMessage.error('获取联系方式失败')
  }
}

const handleComplete = async (claim) => {
  try {
    await ElMessageBox.confirm(
        '确认已从发布者处获得物品？此操作将标记为已归还。',
        '确认归还',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'success'
        }
    )

    await completeClaim(claim.id, user.id)
    ElMessage.success('感谢！物品已标记为已归还')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

const getStatusText = (status) => {
  const map = {
    0: '待确认',
    1: '已同意',
    2: '已拒绝',
    5: '已完成'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    5: 'info'
  }
  return map[status] || ''
}
</script>

<style scoped>
.my-claims-container {
  padding: 16px 24px;
}

.animate-fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

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

.table-card {
  border-radius: 14px !important;
  background-color: #fff;
  border: 1px solid #f1f5f9 !important;
}

.claims-table {
  border-radius: 12px;
  overflow: hidden;
}

.claims-table :deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.claims-table :deep(.el-table__header th) {
  background: transparent !important;
  color: #475569 !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  padding: 16px 0 !important;
  border-bottom: 2px solid #cbd5e1 !important;
}

.claims-table :deep(.el-table__body td) {
  padding: 16px 8px !important;
  vertical-align: middle !important;
}

.claims-table :deep(.el-table__row:hover) {
  background-color: #f8fafc !important;
}

.item-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-icon {
  font-size: 24px;
  color: #4f46e5;
  flex-shrink: 0;
}

.item-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.item-id {
  font-size: 12px;
  color: #94a3b8;
}

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

.status-badge {
  font-weight: 600;
  border-radius: 20px;
  padding: 6px 16px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  align-items: center;
  padding: 4px 0;
}

.action-btn {
  min-width: 120px;
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

.contact-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%);
  border: none;
  color: white;
}

.contact-btn:hover {
  background: linear-gradient(135deg, #4338ca 0%, #3730a3 100%);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.no-action {
  color: #cbd5e1;
  font-size: 18px;
}

/* 联系方式对话框样式 */
:deep(.contact-dialog-content) {
  padding: 8px 0;
}

:deep(.contact-item) {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

:deep(.contact-item:last-of-type) {
  border-bottom: none;
}

:deep(.contact-label) {
  font-size: 14px;
  color: #64748b;
  min-width: 80px;
  font-weight: 500;
}

:deep(.contact-value) {
  font-size: 15px;
  color: #0f172a;
  font-weight: 600;
}

:deep(.contact-value.highlight) {
  color: #4f46e5;
  font-size: 18px;
  font-family: monospace;
}

:deep(.contact-tip) {
  margin-top: 12px;
  padding: 12px;
  background: #f8fafc;
  border-left: 3px solid #4f46e5;
  border-radius: 6px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}
</style>
