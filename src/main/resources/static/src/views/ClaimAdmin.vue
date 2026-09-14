<template>
  <div class="claim-admin-container">
    <div class="page-header">
      <h3 class="page-title">认领申请审核中心</h3>
      <span class="page-tips">管理员专属：请仔细甄别失主的“认领说明凭证”，核实无误后再予通过。</span>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="list" border stripe style="width: 100%;">
        <el-table-column label="申请认领物品" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span style="font-weight: bold; color: #409EFF;">{{ scope.row.itemName || '未知物品' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="认领申请人" width="130" align="center">
          <template #default="scope">
            <el-tag size="small" effect="plain">{{ scope.row.username || '未知学生' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="失主核心认领说明/凭证证明" min-width="220" show-overflow-tooltip/>

        <el-table-column label="申请状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'warning' : scope.row.status === 1 ? 'success' : 'danger'" disable-transitions>
              {{ scope.row.status === 0 ? '审核中' : scope.row.status === 1 ? '已通过' : '已驳回' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="快捷流转操作" width="160" align="center">
          <template #default="scope">
            <div v-if="scope.row.status === 0" class="btn-group">
              <el-button type="success" size="small" @click="handleReview(scope.row.id, 1)">准予认领</el-button>
              <el-button type="danger" size="small" @click="handleReview(scope.row.id, 2)">驳回</el-button>
            </div>
            <span v-else style="color: #909399; font-size: 13px;">已归档</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="list.length === 0" description="当前系统无待处理的认领申请请求" :image-size="120" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getClaimList, reviewClaim } from '../api'
import { ElMessage } from 'element-plus'

const list = ref([])

const loadData = async () => {
  try {
    const res = await getClaimList()
    list.value = res || []
  } catch (error) {
    console.error(error)
  }
}

onMounted(loadData)

const handleReview = async (id, status) => {
  await reviewClaim(id, status)
  if (status === 1) {
    ElMessage.success('认领审核已通过！该物品正式转为“已归还”结案状态。')
  } else {
    ElMessage.warning('已驳回该认领申请，物品重新回滚为“可认领”状态。')
  }
  loadData()
}
</script>

<style scoped>
.claim-admin-container {
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
  border-left: 4px solid #f56c6c;
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