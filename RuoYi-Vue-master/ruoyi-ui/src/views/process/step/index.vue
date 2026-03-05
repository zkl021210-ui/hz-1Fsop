<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="关联型号" prop="modelName">
        <el-input
          v-model="queryParams.modelName"
          placeholder="请输入关联型号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="作业阶段" prop="processStage">
        <el-input
          v-model="queryParams.processStage"
          placeholder="请输入作业阶段"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="步骤顺序" prop="stepOrder">
        <el-input
          v-model="queryParams.stepOrder"
          placeholder="请输入步骤顺序"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="步骤标题" prop="stepTitle">
        <el-input
          v-model="queryParams.stepTitle"
          placeholder="请输入步骤标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="示意图路径" prop="imageUrl">
        <el-input
          v-model="queryParams.imageUrl"
          placeholder="请输入示意图路径"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="YOLO检测目标" prop="detectTarget">
        <el-input
          v-model="queryParams.detectTarget"
          placeholder="请输入YOLO检测目标"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="ROI相对坐标" prop="roiConfig">
        <el-input
          v-model="queryParams.roiConfig"
          placeholder="请输入ROI相对坐标"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['process:step:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['process:step:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['process:step:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['process:step:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stepList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="stepId" />
      <el-table-column label="关联型号" align="center" prop="modelName" />
      <el-table-column label="作业阶段" align="center" prop="processStage" />
      <el-table-column label="步骤顺序" align="center" prop="stepOrder" />
      <el-table-column label="步骤标题" align="center" prop="stepTitle" />
      <el-table-column label="操作指导" align="center" prop="stepDesc" />
      <el-table-column label="示意图路径" align="center" prop="imageUrl" />
      <el-table-column label="YOLO检测目标" align="center" prop="detectTarget" />
      <el-table-column label="ROI相对坐标" align="center" prop="roiConfig" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['process:step:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['process:step:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改SOP步骤配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="关联型号" prop="modelName">
          <el-input v-model="form.modelName" placeholder="请输入关联型号" />
        </el-form-item>
        <el-form-item label="作业阶段" prop="processStage">
          <el-input v-model="form.processStage" placeholder="请输入作业阶段" />
        </el-form-item>
        <el-form-item label="步骤顺序" prop="stepOrder">
          <el-input v-model="form.stepOrder" placeholder="请输入步骤顺序" />
        </el-form-item>
        <el-form-item label="步骤标题" prop="stepTitle">
          <el-input v-model="form.stepTitle" placeholder="请输入步骤标题" />
        </el-form-item>
        <el-form-item label="操作指导" prop="stepDesc">
          <el-input v-model="form.stepDesc" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="示意图路径" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="请输入示意图路径" />
        </el-form-item>
        <el-form-item label="YOLO检测目标" prop="detectTarget">
          <el-input v-model="form.detectTarget" placeholder="请输入YOLO检测目标" />
        </el-form-item>
        <el-form-item label="ROI相对坐标" prop="roiConfig">
          <el-input v-model="form.roiConfig" placeholder="请输入ROI相对坐标" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listStep, getStep, delStep, addStep, updateStep } from "@/api/process/step"

export default {
  name: "Step",
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
      // SOP步骤配置表格数据
      stepList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        modelName: null,
        processStage: null,
        stepOrder: null,
        stepTitle: null,
        stepDesc: null,
        imageUrl: null,
        detectTarget: null,
        roiConfig: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询SOP步骤配置列表 */
    getList() {
      this.loading = true
      listStep(this.queryParams).then(response => {
        this.stepList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        stepId: null,
        modelName: null,
        processStage: null,
        stepOrder: null,
        stepTitle: null,
        stepDesc: null,
        imageUrl: null,
        detectTarget: null,
        roiConfig: null
      }
      this.resetForm("form")
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.stepId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加SOP步骤配置"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const stepId = row.stepId || this.ids
      getStep(stepId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改SOP步骤配置"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.stepId != null) {
            updateStep(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addStep(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const stepIds = row.stepId || this.ids
      this.$modal.confirm('是否确认删除SOP步骤配置编号为"' + stepIds + '"的数据项？').then(function() {
        return delStep(stepIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('process/step/export', {
        ...this.queryParams
      }, `step_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
