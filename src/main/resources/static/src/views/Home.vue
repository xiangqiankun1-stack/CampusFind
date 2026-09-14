<template>
  <div class="home-container animate-fade-in">
    <div class="page-header">
      <div class="header-left">
        <h3 class="page-title">失物招领数据大厅</h3>
        <p class="page-subtitle">实时监控全校物资流转底账，让温暖高效传递</p>
      </div>
      <div class="header-right">
        <el-tag type="info" class="tips-tag" effect="plain">
          <el-icon><InfoFilled /></el-icon> 提示：点击表格中的实物图可查看高清大图
        </el-tag>
      </div>
    </div>

    <el-row v-if="isStatsLoaded" :gutter="20" class="stat-dashboard">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card total-card">
          <div class="card-glow"></div>
          <div class="stat-content">
            <div class="stat-meta">
              <span class="stat-label">总上报物品</span>
              <div class="stat-icon-wrapper"><el-icon><Box /></el-icon></div>
            </div>
            <div class="stat-value">{{ dashboardStats.total }}<span class="unit">件</span></div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card available-card">
          <div class="card-glow"></div>
          <div class="stat-content">
            <div class="stat-meta">
              <span class="stat-label">寻物/招领中</span>
              <div class="stat-icon-wrapper"><el-icon><Search /></el-icon></div>
            </div>
            <div class="stat-value">{{ dashboardStats.available }}<span class="unit">件</span></div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card claiming-card">
          <div class="card-glow"></div>
          <div class="stat-content">
            <div class="stat-meta">
              <span class="stat-label">正在认领核对</span>
              <div class="stat-icon-wrapper"><el-icon><Coordinate /></el-icon></div>
            </div>
            <div class="stat-value">{{ dashboardStats.claiming }}<span class="unit">件</span></div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card success-card">
          <div class="card-glow"></div>
          <div class="stat-content">
            <div class="stat-meta">
              <span class="stat-label">已成功归还</span>
              <div class="stat-icon-wrapper"><el-icon><SuccessFilled /></el-icon></div>
            </div>
            <div class="stat-value">{{ dashboardStats.returned }}<span class="unit">件</span></div>
            <div class="progress-box">
              <el-progress
                  :percentage="dashboardStats.successRate"
                  :stroke-width="5"
                  color="#10b981"
                  :show-text="false"
              />
              <span class="rate-text">结案率 {{ dashboardStats.successRate }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-skeleton v-else :rows="2" animated class="skeleton-holder" />

    <div class="filter-bar">
      <el-input
          v-model="keyword"
          placeholder="搜索物品名称、地点或特征..."
          prefix-icon="Search"
          class="custom-search"
          clearable
      />
      <el-select v-model="typeFilter" placeholder="按物品分类过滤" clearable class="custom-select">
        <el-option label="证件 / 卡券" value="证件"/>
        <el-option label="数码电子" value="数码"/>
        <el-option label="生活日用" value="日用品"/>
        <el-option label="图书资料" value="图书"/>
        <el-option label="钥匙 / 随身饰品" value="饰品"/>
        <el-option label="衣物鞋帽" value="衣物"/>
        <el-option label="其他" value="其他"/>
      </el-select>
    </div>

    <div class="table-wrapper">
      <el-table :data="filteredList" row-class-name="custom-row" style="width: 100%;">
        <el-table-column label="实物缩略图" width="130" align="center">
          <template #default="scope">
            <div class="img-container" v-if="scope.row.image" @click="openPreview(scope.row.image)">
              <el-image :src="scope.row.image" fit="cover" class="table-img" />
              <div class="img-mask"><el-icon><ZoomIn /></el-icon></div>
            </div>
            <span v-else class="no-img-text">📸 暂无图片</span>
          </template>
        </el-table-column>

        <el-table-column label="物品基础档案" min-width="180">
          <template #default="scope">
            <div class="item-profile">
              <span class="item-name">{{ scope.row.name }}</span>
              <div class="badges-row">
                <el-tag 
                  size="small" 
                  :type="scope.row.publishType === 1 ? 'success' : 'danger'" 
                  class="publish-type-badge"
                >
                  {{ scope.row.publishType === 1 ? '🔍 招领' : '📢 寻物' }}
                </el-tag>
                <el-tag size="small" class="item-type-badge">{{ scope.row.type }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="地点 / 时间" min-width="180">
          <template #default="scope">
            <div class="geo-info">
              <div class="info-line"><el-icon class="geo-icon"><Location /></el-icon> {{ scope.row.location }}</div>
              <div class="info-line time"><el-icon class="geo-icon"><Clock /></el-icon> {{ scope.row.time }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="publisherName" label="发布人" width="120" align="center">
          <template #default="scope">
            <el-tag size="small" type="info" class="user-tag">{{ scope.row.publisherName || '匿名同学' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="详细特征备注" min-width="160" show-overflow-tooltip />

        <el-table-column label="流转状态" width="120" align="center">
          <template #default="scope">
            <span v-if="scope.row.status === 0" class="status-dot-badge status-gray">
              待审核
            </span>
            <span v-else-if="scope.row.status === 1" class="status-dot-badge status-green">
              可认领
            </span>
            <span v-else-if="scope.row.status === 2" class="status-dot-badge status-orange">
              认领中
            </span>
            <span v-else-if="scope.row.status === 3" class="status-dot-badge status-blue">
              已归还
            </span>
            <span v-else class="status-dot-badge status-gray">
              未知
            </span>
          </template>
        </el-table-column>

        <el-table-column label="大厅操作" width="140" align="center" fixed="right">
          <template #default="scope">
            <!-- 招领启事：失主可以申请认领 -->
            <el-button
                v-if="user.role === 0 && scope.row.publishType === 1 && scope.row.status === 1"
                type="primary"
                size="small"
                class="claim-action-btn"
                @click="handleClaim(scope.row)"
            >
              申请认领
            </el-button>
            
            <!-- 寻物启事：拾获者可以上报找到了 -->
            <el-button
                v-if="user.role === 0 && scope.row.publishType === 2 && scope.row.status === 1"
                type="warning"
                size="small"
                class="report-action-btn"
                @click="handleReportFound(scope.row)"
            >
              我找到了
            </el-button>
            
            <span v-else-if="scope.row.status === 2" class="claiming-text-badge">
              🔒 {{ scope.row.claimerName || '某同学' }} 核对中
            </span>
            <span v-else-if="scope.row.status === 3" class="returned-text-badge">
              ✅ 已结案
            </span>
            <span v-else class="disable-dash">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="filteredList.length === 0" :image-size="120" description="大厅空空如也，未找到匹配的失物启事" />
    </div>

    <el-dialog v-model="previewVisible" title="实物多维大图" width="40%" destroy-on-close class="custom-dialog">
      <div class="dialog-img-wrapper">
        <img :src="previewUrl" class="dialog-big-img" alt="实物大图" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { getList, claim, getStats } from '../api/index'  // ✅ 注意这里是 ../api
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box, Search, Coordinate, SuccessFilled, InfoFilled, Location, Clock, ZoomIn } from '@element-plus/icons-vue'

const currentUser = ref({})
const list = ref([])
const keyword = ref('')
const typeFilter = ref('')
const isStatsLoaded = ref(false)
const dashboardStats = ref({
  total: 0,
  available: 0,
  claiming: 0,
  returned: 0,
  successRate: 0
})

const previewVisible = ref(false)
const previewUrl = ref('')

const user = computed(() => {
  try {
    const cached = localStorage.getItem('user')
    if (!cached) return { username: 'GUEST', role: 0 }
    const parsed = JSON.parse(cached)
    parsed.role = Number(parsed.role)
    return parsed
  } catch (e) {
    return { username: 'GUEST', role: 0 }
  }
})

const loadData = async () => {
  try {
    console.log('========== 开始加载大厅数据 ==========')
    isStatsLoaded.value = false

    const [resList, resStats] = await Promise.all([
      getList(),
      getStats()
    ])

    console.log('物品列表响应:', resList)
    console.log('统计数据响应:', resStats)
    
    // 🔍 调试：打印第一个物品的完整信息
    if (resList && resList.length > 0) {
      console.log('🔍 第一个物品详情:', resList[0])
      console.log('🔍 publisherName 字段值:', resList[0].publisherName)
      console.log('🔍 userId 字段值:', resList[0].userId)
    }

    list.value = resList || []

    if (resStats) {
      console.log('原始统计数据:', resStats)

      dashboardStats.value = {
        total: resStats.total ?? 0,
        available: resStats.available ?? 0,
        claiming: resStats.claiming ?? 0,
        returned: resStats.returned ?? 0,
        successRate: resStats.successRate ?? 0
      }

      console.log('更新后的仪表盘数据:', dashboardStats.value)

      await nextTick()
      isStatsLoaded.value = true
    }
  } catch (error) {
    console.error('❌ 加载大厅复合数据失败:', error)
    isStatsLoaded.value = true
  }
  console.log('==========================================')
}

onMounted(loadData)
onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
  }
})
const filteredList = computed(() => {
  return list.value.filter(item => {
    const matchName = !keyword.value ||
        item.name?.toLowerCase().includes(keyword.value.toLowerCase()) ||
        item.location?.toLowerCase().includes(keyword.value.toLowerCase())
    const matchType = !typeFilter.value || item.type === typeFilter.value
    return matchName && matchType
  })
})

const openPreview = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

const handleClaim = async (row) => {
  if (!user.value.id) {
    ElMessage.error('请先登录系统！')
    return
  }

  try {
    const { value } = await ElMessageBox.prompt(
        '请输入您认领该物品的强有力凭证（如颜色、失散细节等）',
        '发起失物安全认领',
        {
          confirmButtonText: '立即提交审核',
          cancelButtonText: '暂离',
          inputPattern: /.+/,
          inputErrorMessage: '认领凭证不能为空！'
        }
    )

    await claim({
      itemId: row.id,
      userId: user.value.id,
      description: value
    })

    ElMessage.success('认领请求已安全挂起，请等待系统管理员核验！')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('认领失败:', error)
    }
  }
}

/**
 * 处理"我找到了"按钮点击（针对寻物启事）
 */
const handleReportFound = async (row) => {
  if (!user.value.id) {
    ElMessage.error('请先登录系统！')
    return
  }

  try {
    const { value } = await ElMessageBox.prompt(
        '恭喜！请详细描述您找到该物品的情况（如：在哪里找到的、物品现状、联系方式等）',
        '上报找到物品',
        {
          confirmButtonText: '提交通报',
          cancelButtonText: '暂离',
          inputPattern: /.+/,
          inputErrorMessage: '上报信息不能为空！'
        }
    )

    await claim({
      itemId: row.id,
      userId: user.value.id,
      description: value
    })

    ElMessage.success('上报成功！已通知失主，请等待核实')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('上报失败:', error)
    }
  }
}
</script>

<style scoped>
.home-container {
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
.tips-tag {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
}

/* 📊 指标大盘：轻量高质感双层结构 */
.stat-dashboard {
  margin-bottom: 24px;
}
.stat-card {
  border-radius: 14px !important;
  background-color: #fff;
  border: 1px solid #f1f5f9 !important;
  position: relative;
  overflow: hidden;
}
.card-glow {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 4px;
}
.total-card .card-glow { background: #3b82f6; }
.available-card .card-glow { background: #8b5cf6; }
.claiming-card .card-glow { background: #f59e0b; }
.success-card .card-glow { background: #10b981; }

.stat-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}
.stat-icon-wrapper {
  font-size: 18px;
  padding: 6px;
  border-radius: 8px;
  display: flex;
  align-items: center;
}
.total-card .stat-icon-wrapper { color: #3b82f6; background: #eff6ff; }
.available-card .stat-icon-wrapper { color: #8b5cf6; background: #f5f3ff; }
.claiming-card .stat-icon-wrapper { color: #f59e0b; background: #fffbeb; }
.success-card .stat-icon-wrapper { color: #10b981; background: #ecfdf5; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}
.stat-value .unit {
  font-size: 13px;
  font-weight: 400;
  color: #94a3b8;
  margin-left: 4px;
}
.progress-box {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.progress-box :deep(.el-progress) { flex: 1; }
.rate-text {
  font-size: 12px;
  color: #10b981;
  font-weight: 600;
  white-space: nowrap;
}
.skeleton-holder {
  margin-bottom: 24px;
  background: #fff;
  padding: 24px;
  border-radius: 14px;
}

/* 🔍 过滤工具条流线美化 */
.filter-bar {
  display: flex;
  gap: 12px;
  background-color: #fff;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #f1f5f9;
  margin-bottom: 20px;
}
.custom-search { width: 300px; }
.custom-search :deep(.el-input__wrapper) { border-radius: 8px; background-color: #f8fafc; box-shadow: none !important; border: 1px solid #e2e8f0; }
.custom-select :deep(.el-input__wrapper) { border-radius: 8px; background-color: #f8fafc; box-shadow: none !important; border: 1px solid #e2e8f0; }

/* 📑 数据表格精琢：剔除原生的厚重感与SCSS降级改造 */
.table-wrapper {
  background-color: #fff;
  border-radius: 14px;
  border: 1px solid #f1f5f9;
  padding: 8px;
  overflow: hidden;
}

/* 💡 深度作用选择器平铺——全面修正SCSS语法错误 */
.table-wrapper :deep(.el-table) {
  --el-table-border-color: #f1f5f9;
}
.table-wrapper :deep(.el-table__header) th {
  background-color: #f8fafc !important;
  color: #475569 !important;
  font-weight: 600 !important;
  height: 45px !important;
}

/* 图片悬浮预览遮罩微交互 */
.img-container {
  width: 90px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  border: 1px solid #e2e8f0;
  display: inline-block;
}
.table-img { width: 100%; height: 100%; transition: transform 0.3s; }
.img-mask {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(15, 23, 42, 0.5); color: #fff; font-size: 16px;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.img-container:hover .table-img { transform: scale(1.08); }
.img-container:hover .img-mask { opacity: 1; }
.no-img-text { font-size: 12px; color: #94a3b8; }

/* 物品基础资料融合行 */
.item-profile {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.badges-row {
  display: flex;
  gap: 6px;
  align-items: center;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.publish-type-badge {
  font-weight: 600;
  border-radius: 4px;
}

.item-type-badge {
  font-size: 11px;
  color: #4f46e5;
  background-color: #eef2ff;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

/* 时间与地点组合栏 */
.geo-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #334155;
}
.info-line { display: flex; align-items: center; gap: 4px; }
.info-line.time { color: #64748b; font-size: 12px; }
.geo-icon { color: #94a3b8; }

.user-tag { border-radius: 4px; font-weight: 500; background-color: #f1f5f9; border: none; color: #475569; }

/* 圆角极简状态标签 */
.status-dot-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
  display: inline-block;
}
.status-gray { color: #64748b; background-color: #f1f5f9; }
.status-green { color: #059669; background-color: #ecfdf5; }
.status-orange { color: #d97706; background-color: #fffbeb; }
.status-blue { color: #2563eb; background-color: #eff6ff; }

/* 操作区流线按钮与文本 */
.claim-action-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%) !important;
  border: none !important;
  font-weight: 600;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(79, 70, 229, 0.15);
}
.claim-action-btn:hover { box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25); }

.report-action-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%) !important;
  border: none !important;
  font-weight: 600;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(245, 158, 11, 0.15);
}
.report-action-btn:hover { box-shadow: 0 4px 12px rgba(245, 158, 11, 0.25); }

.claiming-text-badge { font-size: 12px; color: #94a3b8; font-weight: 500; }
.returned-text-badge { font-size: 12px; color: #10b981; font-weight: 600; }
.disable-dash { color: #cbd5e1; }

/* 弹窗圆润化 */
.custom-dialog :deep(.el-dialog) { border-radius: 16px; overflow: hidden; }
.dialog-img-wrapper { text-align: center; padding: 10px 0; }
.dialog-big-img { max-width: 100%; max-height: 65vh; border-radius: 8px; object-fit: contain; }
</style>
