<template>
  <div class="admin-container">
    <div class="page-header">
      <h3 class="page-title">发布申请审核后台</h3>
      <span class="page-tips">管理员专属：请认真核对学生发布的物品信息，确保内容合规真实。</span>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="list" border stripe style="width: 100%;">
        <el-table-column label="物品图片" width="130" align="center">
          <template #default="scope">
            <el-image 
              v-if="scope.row.image" 
              :src="scope.row.image" 
              style="width: 90px; height: 65px; border-radius: 4px; cursor: pointer; box-shadow: 0 2px 4px rgba(0,0,0,0.1);"
              fit="cover"
              @click="openPreview(scope.row.image)"
            />
            <span v-else style="color: #999; font-size: 13px;">📸 暂无图片</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="name" label="物品名称" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="type" label="类型" width="100" align="center"/>
        <el-table-column prop="location" label="拾取/遗失地点" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="time" label="发现时间" width="170" align="center"/>
        <el-table-column prop="description" label="详细描述" min-width="180" show-overflow-tooltip/>

        <el-table-column label="当前状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'warning' : scope.row.status === 1 ? 'success' : 'danger'" disable-transitions>
              {{ scope.row.status === 0 ? '待审核' : scope.row.status === 1 ? '已通过' : '已驳回' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="审核操作" width="160" align="center">
          <template #default="scope">
            <div v-if="scope.row.status === 0" class="btn-group">
              <el-button size="small" type="success" @click="approve(scope.row.id)">通过</el-button>
              <el-button size="small" type="danger" @click="reject(scope.row.id)">驳回</el-button>
            </div>
            <span v-else style="color: #909399; font-size: 13px;">已处理完毕</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="list.length === 0" description="暂无需要审核的物品发布申请" :image-size="120" />
    </el-card>

    <el-dialog v-model="previewVisible" title="图片清晰视图" width="45%" destroy-on-close>
      <div style="text-align: center;">
        <img :src="previewUrl" style="max-width: 100%; max-height: 70vh; object-fit: contain; border-radius: 4px;" alt="放大图" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPending, review } from '../api'
import { ElMessage } from 'element-plus'

const list = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const loadData = async () => {
  try {
    const res = await getPending()
    list.value = res || []
  } catch (error) {
    console.error(error)
  }
}

onMounted(loadData)

const openPreview = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

const approve = async (id) => {
  await review(id, 1) // 1 = 通过
  ElMessage.success('该物品审核已通过，正式上架招领大厅')
  loadData()
}

const reject = async (id) => {
  await review(id, 2) // 2 = 驳回
  ElMessage.warning('已驳回该发布申请')
  loadData()
}
</script>

<style scoped>
.admin-container {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
}
.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-left: 4px solid #e6a23c;
  padding-left: 12px;
}
.page-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
.page-tips {
  font-size: 13px;
  color: #909399;
}
.table-card {
  border: none;
}
.btn-group {
  display: flex;
  gap: 5px;
  justify-content: center;
}
</style>