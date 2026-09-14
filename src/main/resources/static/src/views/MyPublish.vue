<template>
  <div class="my-publish-container">
    <div class="page-header">
      <h3 class="page-title">📦 我的发布记录</h3>
      <span class="page-tips">您可以在此管理自己发布的所有招领/寻物启事</span>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">总发布数</div>
            <div class="stat-value">{{ stats.total }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">待审核</div>
            <div class="stat-value text-warning">{{ stats.pending }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">进行中</div>
            <div class="stat-value text-primary">{{ stats.active }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-label">已结案</div>
            <div class="stat-value text-success">{{ stats.completed }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 物品列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="list" border stripe style="width: 100%;">
        <el-table-column label="实物图" width="100" align="center">
          <template #default="scope">
            <el-image 
              v-if="scope.row.image" 
              :src="scope.row.image" 
              fit="cover" 
              style="width: 60px; height: 60px; border-radius: 4px;"
              :preview-src-list="[scope.row.image]"
            />
            <span v-else class="no-img">📸</span>
          </template>
        </el-table-column>

        <el-table-column label="物品信息" min-width="180">
          <template #default="scope">
            <div class="item-info">
              <div class="item-name">{{ scope.row.name }}</div>
              <div class="badges-row">
                <el-tag 
                  size="small" 
                  :type="scope.row.publishType === 1 ? 'success' : 'danger'"
                >
                  {{ scope.row.publishType === 1 ? '🔍 招领' : '📢 寻物' }}
                </el-tag>
                <el-tag size="small">{{ scope.row.type }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="地点 / 时间" min-width="180">
          <template #default="scope">
            <div class="geo-info">
              <div class="info-line">📍 {{ scope.row.location }}</div>
              <div class="info-line time">🕒 {{ scope.row.time }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              :disabled="scope.row.status === 3"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="list.length === 0" description="您还没有发布过任何物品哦~" :image-size="120" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMyItems, deleteItem as deleteItemApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const user = JSON.parse(localStorage.getItem('user') || '{}')

// 统计数据
const stats = computed(() => {
  const total = list.value.length
  const pending = list.value.filter(item => item.status === 0).length
  const active = list.value.filter(item => item.status === 1 || item.status === 2).length
  const completed = list.value.filter(item => item.status === 3).length
  
  return { total, pending, active, completed }
})

// 加载数据
const loadData = async () => {
  if (!user.id) {
    ElMessage.error('请先登录')
    return
  }

  try {
    const res = await getMyItems(user.id)
    list.value = res || []
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载数据失败')
  }
}

onMounted(loadData)

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '已发布',
    2: '认领中',
    3: '已归还'
  }
  return map[status] || '未知'
}

// 获取状态标签类型
const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'info'
  }
  return map[status] || ''
}

// 删除物品
const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${item.name}"吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteItemApi(item.id, user.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.my-publish-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.page-tips {
  font-size: 13px;
  color: #909399;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-item {
  text-align: center;
  padding: 10px 0;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.text-warning { color: #e6a23c; }
.text-primary { color: #409eff; }
.text-success { color: #67c23a; }

.table-card {
  border-radius: 8px;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.badges-row {
  display: flex;
  gap: 6px;
}

.geo-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.info-line.time {
  font-size: 12px;
  color: #909399;
}

.no-img {
  font-size: 24px;
  color: #dcdfe6;
}
</style>
