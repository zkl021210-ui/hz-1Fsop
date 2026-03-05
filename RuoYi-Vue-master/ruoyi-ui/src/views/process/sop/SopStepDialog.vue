<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="visible"
    width="1100px"
    append-to-body
    :close-on-click-modal="false"
  >
    <div class="step-container">
      <el-alert
        title="标准定义模式：此处配置将作为【智能作业台】的唯一执行标准。请准确填写 AI 识别参数。"
        type="primary"
        show-icon
        :closable="false"
        style="margin-bottom: 15px;"
      />

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAddStep">新增步骤</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-sort" size="mini">保存顺序</el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="stepList" border height="550px" row-key="stepId" stripe>
        <el-table-column label="顺序" prop="stepOrder" width="60" align="center">
          <template slot-scope="scope">
            <el-tag effect="dark">{{ scope.row.stepOrder }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="作业阶段" prop="processStage" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" effect="plain">{{ scope.row.processStage }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="步骤名称" prop="stepTitle" width="180" align="center" style="font-weight: bold;"/>

        <el-table-column label="AI 智能引导配置" min-width="220" align="left">
          <template slot-scope="scope">
            <div v-if="scope.row.detectTarget" style="line-height: 1.8;">
              <el-tag type="warning" size="mini">YOLO目标</el-tag> <span style="font-weight: bold;">{{ scope.row.detectTarget }}</span>
              <br/>
              <el-tag type="info" size="mini">ROI 区域</el-tag> {{ scope.row.roiConfig || '全屏检测' }}
            </div>
            <div v-else style="color: #ccc; font-size: 12px;">无视觉检测项</div>
          </template>
        </el-table-column>

        <el-table-column label="作业台语音/文字指导" prop="stepDesc" show-overflow-tooltip />

        <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEditStep(scope.row)">修改</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color: #F56C6C" @click="handleDeleteStep(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="formTitle" :visible.sync="openForm" width="650px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">

        <el-row>
          <el-col :span="12">
            <el-form-item label="步骤顺序" prop="stepOrder">
              <el-input-number v-model="form.stepOrder" :min="1" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作业阶段" prop="processStage">
              <el-select v-model="form.processStage" placeholder="请选择" style="width: 100%">
                <el-option label="拆卸" value="拆卸"></el-option>
                <el-option label="清洗" value="清洗"></el-option>
                <el-option label="组装" value="组装"></el-option>
                <el-option label="测试" value="测试"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="步骤标题" prop="stepTitle">
          <el-input v-model="form.stepTitle" placeholder="例如：拆卸电机螺丝" />
        </el-form-item>

        <el-form-item label="作业指导内容" prop="stepDesc">
          <el-input v-model="form.stepDesc" type="textarea" :rows="3" placeholder="这段文字将直接显示在工人的屏幕上" />
        </el-form-item>

        <div style="background: #f8f8f9; padding: 10px; border-radius: 4px; margin-bottom: 20px;">
          <div style="font-weight: bold; margin-bottom: 10px; color: #606266;"><i class="el-icon-camera"></i> 智能视觉配置 (AI)</div>
          <el-row>
            <el-col :span="12">
              <el-form-item label="YOLO 类别名" prop="detectTarget" label-width="100px">
                <el-input v-model="form.detectTarget" placeholder="如: screw_m6" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="ROI 坐标" prop="roiConfig" label-width="90px">
                <el-input v-model="form.roiConfig" placeholder="x,y,w,h" />
              </el-form-item>
            </el-col>
          </el-row>
          <div style="font-size: 12px; color: #909399; padding-left: 20px;">
            * 填写此项后，作业台切到此步骤时，会自动调用摄像头检测指定零件。
          </div>
        </div>

        <el-form-item label="示意图URL" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="图片地址" />
        </el-form-item>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitStepForm">确 定 保 存</el-button>
        <el-button @click="openForm = false">取 消</el-button>
      </div>
    </el-dialog>

    <div slot="footer" class="dialog-footer">
      <el-button @click="visible = false">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
// 引用真实接口
import { listStep, delStep, addStep, updateStep } from "@/api/process/step";

export default {
  name: "SopStepDialog",
  data() {
    return {
      visible: false,
      openForm: false,
      loading: false,
      dialogTitle: "",
      formTitle: "",

      // 当前选中的型号 (核心)
      currentModel: {},

      stepList: [],
      form: {},
      rules: {
        stepOrder: [{ required: true, message: "顺序必填", trigger: "blur" }],
        stepTitle: [{ required: true, message: "标题必填", trigger: "blur" }]
      }
    };
  },
  methods: {
    // 【1. 入口】父组件把 row (包含 dictValue/modelCode) 传进来
    show(modelRow) {
      this.currentModel = modelRow;
      // 假设 modelRow.dictValue 就是 "ZD6" 这种唯一编码
      const code = modelRow.dictValue || modelRow.modelCode;
      const name = modelRow.dictLabel || modelRow.modelName;

      this.dialogTitle = `作业流程配置 - [ ${name} (${code}) ]`;
      this.visible = true;

      this.getList();
    },

    // 【2. 查询】只查当前型号的数据
    getList() {
      this.loading = true;
      const code = this.currentModel.dictValue || this.currentModel.modelCode;

      // 后端查询参数：modelCode = 'ZD6'
      listStep({ modelCode: code }).then(response => {
        this.stepList = response.rows;
        this.loading = false;
      });
    },

    // 各种按钮事件...
    handleAddStep() {
      this.resetForm();
      this.formTitle = "新增步骤";
      this.openForm = true;
    },
    handleEditStep(row) {
      this.resetForm();
      this.form = { ...row };
      this.formTitle = "修改步骤";
      this.openForm = true;
    },
    handleDeleteStep(row) {
      this.$modal.confirm('确认删除？').then(() => {
        return delStep(row.stepId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      });
    },

    // 【3. 核心保存】将数据写入新数据库
    submitStepForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 关键：把当前步骤死死绑定在型号上
          // 这样以后作业台查 'ZD6' 就能查到这条数据
          this.form.modelCode = this.currentModel.dictValue || this.currentModel.modelCode;

          if (this.form.stepId != null) {
            updateStep(this.form).then(() => {
              this.$modal.msgSuccess("保存成功");
              this.openForm = false;
              this.getList();
            });
          } else {
            addStep(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.openForm = false;
              this.getList();
            });
          }
        }
      });
    },

    resetForm() {
      this.form = {
        stepId: null,
        stepOrder: this.stepList.length + 1,
        processStage: "拆卸",
        stepTitle: "",
        detectTarget: "", // AI
        roiConfig: "",    // AI
        stepDesc: "",
        imageUrl: ""
      };
    }
  }
};
</script>
