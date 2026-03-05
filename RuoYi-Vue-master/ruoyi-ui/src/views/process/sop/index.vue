<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="设备型号" prop="modelName">
        <el-input v-model="queryParams.modelName" placeholder="输入型号名称搜索" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-plus" type="primary" plain @click="handleAdd">新增设备型号</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="modelList" border stripe>
      <el-table-column label="序号" type="index" width="80" align="center"/>

      <el-table-column label="型号标识" prop="modelCode" align="center" width="150">
        <template slot-scope="scope">
          <el-tag effect="dark">{{ scope.row.modelCode }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="设备名称" prop="modelName" align="center" style="font-weight: bold;"/>

      <el-table-column label="描述/备注" prop="remark" align="center" show-overflow-tooltip/>

      <el-table-column label="SOP 作业配置" align="center" width="300">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            icon="el-icon-s-operation"
            @click="handleConfigSOP(scope.row)"
          >配置作业流程</el-button>

          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >修改信息</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            style="color: #f56c6c;"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="型号标识" prop="modelCode">
          <el-input v-model="form.modelCode" placeholder="唯一标识，如：ZD6" />
        </el-form-item>
        <el-form-item label="设备名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="如：ZD6型转辙机" />
        </el-form-item>
        <el-form-item label="备注描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitModelForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </div>
    </el-dialog>

    <sop-step-dialog ref="sopDialog" />
  </div>
</template>

<script>
import SopStepDialog from "./SopStepDialog";

export default {
  name: "SopModelIndex",
  components: { SopStepDialog },
  data() {
    return {
      loading: false,
      showSearch: true,
      modelList: [], // 设备数据列表
      open: false,
      title: "",
      queryParams: { modelName: undefined },
      form: {},
      rules: {
        modelCode: [{ required: true, message: "型号标识不能为空", trigger: "blur" }],
        modelName: [{ required: true, message: "设备名称不能为空", trigger: "blur" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 模拟获取设备列表 (实际请替换为您的后端API)
    getList() {
      this.loading = true;
      // 模拟数据
      setTimeout(() => {
        if (this.modelList.length === 0) {
          this.modelList = [
            { id: 101, modelCode: "ZD6", modelName: "ZD6型转辙机", remark: "既有线常用设备" },
            { id: 102, modelCode: "S700K", modelName: "S700K型转辙机", remark: "提速道岔使用" }
          ];
        }
        this.loading = false;
      }, 500);
    },
    // 打开“新增型号”弹窗
    handleAdd() {
      this.form = {};
      this.open = true;
      this.title = "新增设备型号";
    },
    // 打开“修改型号”弹窗
    handleUpdate(row) {
      this.form = { ...row };
      this.open = true;
      this.title = "修改设备型号";
    },
    // 提交型号数据
    submitModelForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 模拟保存
          if (!this.form.id) {
            this.form.id = new Date().getTime(); // 生成假ID
            this.modelList.push(this.form);
          } else {
            // 找到并更新
            const index = this.modelList.findIndex(item => item.id === this.form.id);
            this.$set(this.modelList, index, this.form);
          }
          this.$modal.msgSuccess("保存成功");
          this.open = false;
        }
      });
    },
    // 删除型号
    handleDelete(row) {
      this.$modal.confirm('删除该型号将同步清空其下的SOP步骤，确认删除？').then(() => {
        const index = this.modelList.indexOf(row);
        this.modelList.splice(index, 1);
        this.$modal.msgSuccess("删除成功");
      });
    },
    // 【核心】点击配置 SOP 流程
    handleConfigSOP(row) {
      // 调用子组件的 show 方法，把当前行的 modelCode 传进去
      // 这样子组件就知道它现在是在给谁配流程
      this.$refs.sopDialog.show(row);
    }
  }
};
</script>
