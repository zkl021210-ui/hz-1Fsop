<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="设备编号" prop="serialNumber">
        <el-input v-model="queryParams.serialNumber" placeholder="请输入设备编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="型号" prop="modelCode">
        <el-input v-model="queryParams.modelCode" placeholder="请输入型号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="工人姓名" prop="workerName">
        <el-input v-model="queryParams.workerName" placeholder="请输入工人姓名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['process:log:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['process:log:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="logId" width="60" />
      <el-table-column label="设备编号" align="center" prop="serialNumber" />
      <el-table-column label="型号" align="center" prop="modelCode" />
      <el-table-column label="作业阶段/步骤" align="center" prop="processStage" min-width="150" show-overflow-tooltip/>
      <el-table-column label="工人姓名" align="center" prop="workerName" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status == '3'" type="warning">暂停</el-tag>
          <el-tag v-else-if="scope.row.status == '2'" type="success">完成</el-tag>
          <el-tag v-else-if="scope.row.status == '1'" type="primary" effect="plain">进行中</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作回放" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.videoUrl"
            size="mini"
            type="text"
            icon="el-icon-video-play"
            @click="handlePlay(scope.row)"
          >回放</el-button>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['process:log:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body @close="closeDialog">
      <video v-if="open" :src="videoUrl" controls autoplay style="width: 100%;"></video>
    </el-dialog>

  </div>
</template>

<script>
import { listLog, delLog } from "@/api/process/log"

export default {
  name: "Log",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      logList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serialNumber: null,
        modelCode: null,
        workerName: null,
        orderByColumn: 'createTime',
        isAsc: 'desc'
      },

      // [新增] 视频播放相关的状态
      open: false,
      title: "",
      videoUrl: "",

    }
  },
  created() {
    this.getList()
  },
  activated() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true
      listLog(this.queryParams).then(response => {
        this.logList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.logId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const logIds = row.logId || this.ids
      this.$modal.confirm('是否确认删除数据？').then(function() {
        return delLog(logIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('process/log/export', { ...this.queryParams }, `log_${new Date().getTime()}.xlsx`)
    },

    // [新增] 视频播放方法
    handlePlay(row) {
      this.title = "操作回放 - 设备: " + row.serialNumber;
      // 拼接完整的URL (process.env.VUE_APP_BASE_API 是 Ruoyi 前端配置的后端基准地址)
      this.videoUrl = process.env.VUE_APP_BASE_API + row.videoUrl;
      this.open = true;
    },
    // [新增] 关闭弹窗清理
    closeDialog() {
      this.videoUrl = "";
    }
  }
}
</script>
