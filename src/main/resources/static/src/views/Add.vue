<template>
  <div class="add-container animate-fade-in">
    <div class="page-header">
      <h3 class="page-title"> 发布物品档案</h3>
      <p class="page-subtitle">填写真实有效的失物/招领信息，让温暖高效传递</p>
    </div>

    <el-card shadow="never" class="form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" class="custom-form">
        
        <!--  发布类型选择 -->
        <el-form-item label="发布类型" prop="publishType">
          <el-radio-group v-model="form.publishType" size="large">
            <el-radio :label="1" border>
              <span class="radio-label">
                <el-icon color="#67c23a"><Search /></el-icon>
                🔍 我捡到了东西（招领启事）
              </span>
            </el-radio>
            <el-radio :label="2" border>
              <span class="radio-label">
                <el-icon color="#f56c6c"><Bell /></el-icon>
                📢 我丢了东西（寻物启事）
              </span>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="物品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物品名称" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="物品分类" prop="type">
          <el-select v-model="form.type" placeholder="请选择物品分类" style="width: 100%;">
            <el-option label="数码" value="数码" />
            <el-option label="证件" value="证件" />
            <el-option label="图书" value="图书" />
            <el-option label="日用品" value="日用品" />
            <el-option label="服饰" value="服饰" />
            <el-option label="饰品" value="饰品" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="发现/丢失地点" prop="location">
          <el-input v-model="form.location" placeholder="例如：图书馆三楼、食堂二楼" />
        </el-form-item>

        <el-form-item label="时间" prop="time">
          <el-date-picker
              v-model="form.time"
              type="datetime"
              placeholder="选择日期时间"
              style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="核心特征" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述物品特征，帮助准确匹配（至少5个字）"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>

        <!-- 🆕 新增联系方式 -->
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="form.contactInfo" placeholder="请输入手机号或微信号" maxlength="20" />
          <div class="form-tip">💡 仅对方可见，用于私下联系</div>
        </el-form-item>

        <el-form-item label="实物照片">
          <el-upload
              class="upload-demo"
              action="/item/upload"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              :show-file-list="false"
              accept="image/*"
          >
            <img v-if="form.image" :src="form.image" class="uploaded-preview" alt="预览图" />
            <div v-else class="upload-placeholder">
              <el-icon class="el-icon--upload"><Plus /></el-icon>
              <div class="el-upload__text">点击或拖拽上传实物照片</div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" size="large" class="submit-btn" :loading="isSubmitting">
            {{ isSubmitting ? '正在提交...' : '立即发布物品档案' }}
          </el-button>
          <el-button @click="handleReset" size="large" class="reset-btn">重置表单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 🆕 智能匹配结果展示区 -->
    <el-card v-if="showMatches || isMatching" shadow="hover" class="match-result-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><MagicStick /></el-icon>
            <span class="header-title">智能匹配结果</span>
            <el-badge v-if="matchResults.length > 0" :value="matchResults.length" class="result-badge" />
          </div>
          <el-tag v-if="matchResults.length > 0" type="success" effect="plain" size="small">
            {{ matchResults.length }} 个相似物品
          </el-tag>
        </div>
      </template>

      <div v-if="isMatching" class="matching-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在智能匹配中...</span>
      </div>

      <el-table v-else :data="matchResults" border stripe max-height="400" class="match-table">
        <el-table-column label="物品名称" width="180" align="center">
          <template #default="scope">
            <div class="item-name-cell">
              <el-icon class="item-icon"><Box /></el-icon>
              <span>{{ scope.row.item?.name || '未知' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="相似度" width="220" align="center">
          <template #default="scope">
            <div class="similarity-cell">
              <el-progress 
                :percentage="scope.row.similarity" 
                :color="getProgressColor(scope.row.similarity)"
                :stroke-width="14"
                class="match-progress"
              />
              <span class="similarity-text">{{ scope.row.similarity }}%</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="匹配原因" min-width="250" show-overflow-tooltip>
          <template #default="scope">
            <div class="reason-cell">
              <el-icon class="reason-icon"><ChatDotRound /></el-icon>
              <span>{{ scope.row.matchReason || '暂无说明' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="地点" width="180" align="center">
          <template #default="scope">
            <div class="location-cell">
              <el-icon class="location-icon"><Location /></el-icon>
              <span>{{ scope.row.item?.location || '未知' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                type="info" 
                size="default" 
                plain
                @click="viewItemDetail(scope.row.item)"
                class="action-btn"
              >
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button 
                type="primary" 
                size="default"
                @click="handleClaimFromMatch(scope.row.item)"
                class="action-btn primary-btn"
              >
                <el-icon><Bell /></el-icon>
                申请认领
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!isMatching && matchResults.length === 0 && form.name" description="暂无相似物品" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { addItem, smartMatch, claim as addClaim } from '../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Search, Bell, MagicStick, Loading, Box, Location, ChatDotRound } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const isSubmitting = ref(false)
const matchResults = ref([])
const showMatches = ref(false)
const isMatching = ref(false)
let matchTimer = null

const form = reactive({
  publishType: 1,
  name: '',
  type: '',
  location: '',
  time: '',
  description: '',
  contactInfo: '',
  image: '',
  userId: null
})

const rules = {
  publishType: [{ required: true, message: '请选择发布类型', trigger: 'change' }],
  name: [
    { required: true, message: '请填写物品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择物品分类', trigger: 'change' }],
  location: [{ required: true, message: '请填写地点', trigger: 'blur' }],
  time: [{ required: true, message: '请选择时间', trigger: 'change' }],
  description: [
    { required: true, message: '请填写核心特征描述', trigger: 'blur' },
    { min: 5, message: '描述至少需要 5 个字', trigger: 'blur' }
  ],
  contactInfo: [
    { required: true, message: '请填写联系方式', trigger: 'blur' },
    { pattern: /^[\w-]{5,20}$/, message: '请输入有效的手机号或微信号', trigger: 'blur' }
  ]
}

watch(
  () => [form.name, form.type, form.location, form.publishType],
  ([newName, newType, newLocation, newPublishType]) => {
    if (matchTimer) {
      clearTimeout(matchTimer)
    }

    if (!newName || !newType) {
      showMatches.value = false
      matchResults.value = []
      return
    }

    matchTimer = setTimeout(async () => {
      isMatching.value = true
      try {
        const matches = await smartMatch(form)
        console.log('✅ 匹配结果:', matches)
        matchResults.value = matches || []
        showMatches.value = matchResults.value.length > 0
      } catch (error) {
        console.error('❌ 匹配失败:', error)
      } finally {
        isMatching.value = false
      }
    }, 500)
  },
  { deep: true }
)

const handleUploadSuccess = (res) => {
  form.image = res.data
  ElMessage.success('实物快照已安全挂接！')
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// Add.vue 的 handleSubmit 方法中
const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 1. 获取用户信息
      const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
      
      // 2. 将用户信息加入到提交的表单数据中
      const submitForm = {
        ...form,
        userId: userInfo.id,         // 传递 ID
        publisherName: userInfo.name // 传递名称（具体字段名需与后端一致）
      };

      isSubmitting.value = true;
      try {
        // 3. 将包含用户信息的数据发送给后端
        const res = await addItem(submitForm); 
        ElMessage.success('🎉 发布成功！');
        // ...后续逻辑
      } catch (error) {
        // ...
      } finally {
        isSubmitting.value = false;
      }
    }
  });
}
const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.image = ''
  matchResults.value = []
  showMatches.value = false
}

const getProgressColor = (similarity) => {
  if (similarity >= 80) return '#67c23a'
  if (similarity >= 60) return '#e6a23c'
  return '#f56c6c'
}

const viewItemDetail = (item) => {
  router.push({
    path: '/home',
    query: { highlight: item.id }
  })
}

const handleClaimFromMatch = async (matchedItem) => {
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      ElMessage.warning('请先登录！')
      return
    }
    const user = JSON.parse(userStr)

    await ElMessageBox.confirm(
        `确定要申请认领「${matchedItem.name}」吗？\n\n拾获者将看到您的联系方式。`,
        '确认申请',
        {
          confirmButtonText: '✅ 确认申请',
          cancelButtonText: '取消',
          type: 'info'
        }
    )

    const claimData = {
      itemId: matchedItem.id,
      userId: user.id,
      description: `我在 ${matchedItem.location} 丢失了 ${matchedItem.name}，特征：${matchedItem.description}`,
      status: 0
    }

    try {
      const res = await addClaim(claimData)
      
      console.log('✅ 认领申请响应:', res)
      
      if (res && typeof res === 'string') {
        ElMessage.success(' ' + res)
        showMatches.value = false
        matchResults.value = []
      } else {
        ElMessage.success('🎉 申请成功！请等待发布者确认')
        showMatches.value = false
        matchResults.value = []
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error(' 申请认领失败:', error)
        ElMessage.error('申请失败，请稍后重试')
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('申请认领失败:', error)
      ElMessage.error('申请失败，请稍后重试')
    }
  }
}

</script>

<style scoped>
.add-container {
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

.form-card {
  border-radius: 14px !important;
  background-color: #fff;
  border: 1px solid #f1f5f9 !important;
  margin-bottom: 24px;
}

.custom-form {
  padding: 10px 20px;
}

:deep(.el-radio.is-bordered) {
  padding: 12px 16px;
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s;
  margin-right: 12px;
  margin-bottom: 8px;
}

:deep(.el-radio.is-bordered.is-checked) {
  border-color: #4f46e5;
  background-color: #f5f3ff;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
}

.upload-demo {
  width: 100%;
}
.upload-placeholder {
  padding: 30px 0;
  text-align: center;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.3s;
}
.upload-placeholder:hover {
  border-color: #4f46e5;
}
.uploaded-preview {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.submit-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%) !important;
  border: none !important;
  font-weight: 600;
  border-radius: 8px !important;
  padding: 12px 32px !important;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
}
.submit-btn:hover {
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.3);
  transform: translateY(-1px);
}

.reset-btn {
  border-radius: 8px !important;
  padding: 12px 32px !important;
}

/* 🆕 智能匹配结果卡片 */
.match-result-card {
  border-radius: 14px !important;
  background-color: #fff;
  border: 1px solid #f1f5f9 !important;
  margin-top: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 20px;
  color: #4f46e5;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.result-badge {
  margin-left: 8px;
}

.matching-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px;
  color: #e6a23c;
  font-size: 14px;
}

.matching-loading .is-loading {
  animation: rotating 1s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.match-reason {
  font-size: 13px;
  color: #64748b;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

/* 🎯 匹配结果表格美化 */
.match-table {
  border-radius: 12px;
  overflow: hidden;
}

.match-table :deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.match-table :deep(.el-table__header th) {
  background: transparent !important;
  color: #475569 !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  padding: 16px 0 !important;
  border-bottom: 2px solid #cbd5e1 !important;
}

.match-table :deep(.el-table__body td) {
  padding: 16px 8px !important;
  vertical-align: middle !important;
}

.match-table :deep(.el-table__row:hover) {
  background-color: #f8fafc !important;
}

.item-name-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 600;
  color: #0f172a;
  font-size: 15px;
}

.item-icon {
  font-size: 18px;
  color: #4f46e5;
}

.similarity-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.match-progress {
  width: 100%;
  max-width: 160px;
}

.similarity-text {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.reason-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 4px 0;
}

.reason-icon {
  font-size: 16px;
  color: #4f46e5;
  flex-shrink: 0;
  margin-top: 2px;
}

.reason-cell span {
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.location-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #475569;
  font-size: 14px;
}

.location-icon {
  font-size: 16px;
  color: #ef4444;
}

/*  操作按钮区域美化 */
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

.primary-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%);
  border: none;
  color: white;
}

.primary-btn:hover {
  background: linear-gradient(135deg, #4338ca 0%, #3730a3 100%);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
    gap: 8px;
  }
  
  .action-btn {
    width: 100%;
  }
}
</style>
