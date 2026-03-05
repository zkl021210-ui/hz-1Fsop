<template>
  <div class="app-container workbench-container">

    <div class="top-bar">

      <div class="bar-section left">
        <span class="label">当前作业</span>
        <span class="value main-title">{{ taskInfo.deviceType || taskInfo.modelCode || '未开始' }}</span>
      </div>

      <div class="bar-section center">
        <span class="label">设备编号 SN</span>
        <span class="value sn-text">{{ taskInfo.deviceSn || '---' }}</span>

        <el-tag v-if="taskInfo.assemblyRound > 1" type="danger" effect="dark" size="medium" style="margin-left: 10px;">
          返修 (第{{ taskInfo.assemblyRound -1}}次)
        </el-tag>
        <el-tag v-else-if="taskInfo.assemblyRound == 1" type="primary" effect="light" size="medium" style="margin-left: 10px;">
          首次装配
        </el-tag>

        <div class="recording-status" v-if="isRecording">
          <span class="red-dot"></span>
          <span class="rec-text">REC</span>
          <span class="rec-time">{{ recordingTimer }}</span>
        </div>
      </div>

      <div class="bar-section right">
        <el-button
          v-if="isWorking"
          type="danger"
          icon="el-icon-video-pause"
          class="big-pause-btn"
          @click="handlePause"
        >
          暂 停 任 务
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" style="margin-top: 20px; height: calc(100vh - 160px);">

      <el-col :span="16" style="height: 100%;">
        <el-card class="visual-card" body-style="padding:0; height:100%; display:flex; flex-direction:column;">

          <div slot="header" class="clean-header">
            <div class="header-left">
              <i class="el-icon-video-camera" style="color: #409EFF; margin-right: 5px;"></i>
              <span style="font-weight: bold; font-size: 16px; color: #303133;">实时作业监控</span>
            </div>
            <el-tag type="success" effect="plain" size="small">AI 视觉引擎：在线</el-tag>
          </div>

          <div class="video-box" style="height: 580px; position: relative; overflow: hidden; background: #000;">
            <img
              src="http://localhost:5000/video_feed"
              style="width: 100%; height: 100%; object-fit: contain;"
              onerror="this.style.display='none'"
            />
            <div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #666; pointer-events: none; z-index: -1;">
              <i class="el-icon-loading" style="font-size: 40px;"></i>
              <p>等待视频信号...</p>
            </div>
          </div>

          <div class="process-flow-bar">
            <div class="custom-steps">
              <div
                v-for="(item, index) in stepList"
                :key="index"
                class="step-item"
                :class="{
                  'is-finished': index < currentStepIndex,
                  'is-current': index === currentStepIndex,
                  'is-wait': index > currentStepIndex
                }"
              >
                <div class="step-capsule">
                  <i v-if="index < currentStepIndex" class="el-icon-check" style="font-weight:bold; margin-right:4px;"></i>
                  <span class="step-name">{{ item.stepName || item.stepTitle || ('步骤' + (index + 1)) }}</span>
                </div>

                <div v-if="index < stepList.length - 1" class="step-line"></div>
              </div>
            </div>
          </div>

        </el-card>
      </el-col>

      <el-col :span="8" style="height: 100%;">
        <el-card class="sop-card" body-style="height: calc(100% - 60px); overflow-y: auto;">
          <div slot="header" class="clean-header">
            <span style="font-weight: bold; color: #303133;">标准作业指导 (SOP)</span>
            <el-progress
              :percentage="progressPercentage"
              :status="isFinished ? 'success' : null"
              :stroke-width="14"
              text-inside
              style="width: 120px;"
            ></el-progress>
          </div>

          <div v-if="!isWorking && !isFinished" class="empty-state">
            <i class="el-icon-lock" style="font-size: 60px; color: #ddd;"></i>
            <p style="color: #bbb; margin-top: 20px;">请先完成开工登记</p>
          </div>

          <div v-else-if="isWorking && currentStep" class="sop-content" :key="currentStepIndex">
            <div class="step-header">
              <h2 class="step-index">STEP {{ currentStepIndex + 1 }}</h2>
              <h3 class="step-title">{{ currentStep.stepName || currentStep.stepTitle || '未命名步骤' }}</h3>
            </div>

            <div class="target-bar" v-if="currentStep.detectTarget">
              <i class="el-icon-aim" style="color: #409EFF; margin-right:5px;"></i>
              AI 视觉引擎监测中：<span class="target-highlight">{{ currentStep.detectTarget }}</span>
            </div>

            <div class="sop-desc full-height">
              <h4><i class="el-icon-document"></i> 操作要点：</h4>
              <div class="desc-text-wrapper">
                <p class="desc-text">{{ currentStep.stepDesc || '请按照标准工艺进行操作。' }}</p>
              </div>
            </div>

            <div class="action-area">
              <el-button
                v-if="currentStepIndex > 0"
                type="info"
                plain
                icon="el-icon-arrow-left"
                class="prev-btn"
                @click="handlePrevStep"
                :disabled="loadingStep"
              >
                上一步
              </el-button>

              <el-button
                type="primary"
                class="next-btn"
                @click="handleNextStep"
                :loading="loadingStep"
              >
                {{ loadingStep ? '提交中...' : '人工确认完成' }}
                <i class="el-icon-check el-icon--right"></i>
              </el-button>
            </div>
          </div>

          <div v-else class="finish-state">
            <i class="el-icon-circle-check" style="font-size: 80px; color: #67C23A;"></i>
            <h2>装配完成！</h2>
            <el-button type="success" @click="finishWork" style="margin-top: 20px;">归档并开始下一台</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      title="🛠️ 开工登记"
      :visible.sync="openDialog"
      width="480px"
      center
      :close-on-click-modal="false"
      :show-close="false"
      :close-on-press-escape="false"
      custom-class="start-dialog"
    >
      <el-form :model="startForm" ref="startForm" label-width="100px" style="padding-right: 20px;">
        <el-form-item label="转辙机型号" prop="deviceType">
          <el-select v-model="startForm.deviceType" placeholder="请选择型号" style="width: 100%">
            <el-option
              v-for="item in modelList"
              :key="item.modelId"
              :label="item.modelName"
              :value="item.modelCode || item.modelName"
            >
              <span style="float: left">{{ item.modelName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.modelCode || item.modelName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="作业人员" prop="workerId">
          <el-select v-model="startForm.workerId" placeholder="请选择" style="width: 100%" filterable>
            <el-option v-for="w in workerList" :key="w.workerId" :label="w.workerName" :value="w.workerId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="设备条码" prop="deviceSn">
          <el-input ref="snInput" v-model="startForm.deviceSn" placeholder="扫码或输入" @keyup.enter.native="handleStartWork" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleCancel">取 消</el-button>
        <el-button type="primary" @click="handleStartWork" :loading="loadingStart">确 认 开 工</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import { startTask, listTask, updateTask } from "@/api/process/task";
import { listStep } from "@/api/process/step";
import { listWorker } from "@/api/process/worker";
import { listModel } from "@/api/process/model";
import { addLog } from "@/api/process/log";
// ✅ 引入新的聚合接口
import { startSopStep, stopSopStep } from "@/api/process/sop";

export default {
  name: "Workbench",
  data() {
    return {
      // 弹窗与加载状态
      openDialog: true,
      isWorking: false,
      isFinished: false,
      loadingStep: false,
      loadingStart: false,
      isRequestingStart: false,

      // 基础数据
      workerList: [],
      modelList: [],
      stepList: [],
      currentStepIndex: 0,

      // 任务信息
      taskInfo: { taskId: null, deviceSn: '', deviceType: '', workerName: '', status: '0' },
      startForm: { deviceType: '', deviceSn: '', workerId: null },
      sessionStartTime: null,

      // 轮询定时器
      syncTimer: null,

      // 录像相关 UI 状态
      isRecording: false,
      recordingTimer: "00:00",
      recordInterval: null,
      recordSeconds: 0,

      // 🔥 [关键] 存储当前步骤在数据库的 logId (用于停止录像时回传)
      currentLogId: null
    };
  },
  computed: {
    currentStep() { return (this.stepList && this.stepList.length > 0) ? this.stepList[this.currentStepIndex] : null; },
    progressPercentage() {
      if (!this.stepList || this.stepList.length === 0) return 0;
      if (this.isFinished) return 100;
      return Math.round((this.currentStepIndex / this.stepList.length) * 100);
    }
  },
  mounted() {
    this.initBaseData();
    // 自动聚焦输入框
    this.$nextTick(() => { if (this.$refs.snInput) this.$refs.snInput.focus(); });
  },
  beforeDestroy() {
    this.stopSync();
    this.stopRecordTimer();
  },

  methods: {
    initBaseData() {
      listWorker().then(res => { this.workerList = res.rows; });
      listModel({pageNum: 1, pageSize: 100}).then(res => {
        this.modelList = res.rows;
        if (this.modelList.length > 0) {
          const firstModel = this.modelList[0];
          this.startForm.deviceType = firstModel.modelCode || firstModel.modelName;
        }
      });
    },

    // ---------------------------------------------------------------
    // 🎨 UI 辅助方法 (纯前端效果，不发网络请求)
    // ---------------------------------------------------------------
    setRecordingUI(isRecording) {
      if (isRecording) {
        this.isRecording = true;
        this.startRecordTimer();
      } else {
        this.isRecording = false;
        this.stopRecordTimer();
      }
    },

    startRecordTimer() {
      this.recordSeconds = 0;
      this.recordingTimer = "00:00";
      if (this.recordInterval) clearInterval(this.recordInterval);
      this.recordInterval = setInterval(() => {
        this.recordSeconds++;
        const m = Math.floor(this.recordSeconds / 60).toString().padStart(2, '0');
        const s = (this.recordSeconds % 60).toString().padStart(2, '0');
        this.recordingTimer = `${m}:${s}`;
      }, 1000);
    },
    stopRecordTimer() {
      if (this.recordInterval) clearInterval(this.recordInterval);
      this.recordInterval = null;
    },

    // ---------------------------------------------------------------
    // 🔗 核心逻辑：与 Java 后端交互 (录像与存库)
    // ---------------------------------------------------------------

    // 修改 triggerStepStart 方法
    triggerStepStart() {
      if (this.currentLogId || this.isRequestingStart) return;
      if (!this.currentStep) return;

      this.isRequestingStart = true;

      // 构造 Payload
      const payload = {
        deviceSn: this.taskInfo.deviceSn,
        stepId: this.currentStep.stepId,
        stepOrder: this.currentStep.stepOrder, // [核心修改] 新增 stepOrder
        stepName: this.currentStep.stepName,
        // 关键：传入轮次，用于后端确定唯一性
        assemblyRound: this.taskInfo.assemblyRound || 1,
        workerName: this.taskInfo.workerName,
        // 传入 AI 配置参数
        modelCode: this.taskInfo.modelCode,
        target: this.currentStep.detectTarget
      };

      startSopStep(payload).then(res => {
        // 后端返回的可能是新 ID，也可能是复用的旧 ID
        console.log("✅ 步骤启动成功, LogID:", res.data);
        this.currentLogId = res.data;
        this.setRecordingUI(true);
      }).finally(() => {
        this.isRequestingStart = false;
      });
    },

    triggerStepStop(status = '2') {
      if (!this.currentLogId) return Promise.resolve();

      const stopPayload = {
        id: this.currentLogId,
        deviceSn: this.taskInfo.deviceSn,
        status: status // ✅ 透传状态给后端
      };

      return stopSopStep(stopPayload).then(() => {
        this.setRecordingUI(false);
        this.currentLogId = null;
      });
    },

    // 辅助：生成发给后端的 AI 配置参数 (复原了您原本 updatePythonConfig 的逻辑)
    getPythonConfigPayload() {
        const currentStep = this.currentStep;
        const currentIndex = this.currentStepIndex;
        let nextTarget = null;
        if (currentIndex < this.stepList.length - 1) {
            nextTarget = this.stepList[currentIndex + 1].detectTarget;
        }

        return {
            stepId: currentStep.stepId,
            modelCode: this.taskInfo.modelCode || this.startForm.deviceType,
            deviceSn: this.taskInfo.deviceSn || this.startForm.deviceSn,
            target: currentStep.detectTarget,
            nextTarget: nextTarget,
            container: currentStep.detectContainer || 'housing'
        };
    },

    // ---------------------------------------------------------------
        // 🖱️ 按钮事件处理 (修正版)
        // ---------------------------------------------------------------

        // 【开工按钮】
        handleStartWork() {
          const selectedModelCode = this.startForm.deviceType;
          if (!selectedModelCode) return this.$message.warning("请选择型号！");
          if (!this.startForm.workerId) return this.$message.warning("请选择人员！");
          if (!this.startForm.deviceSn) { this.$message.warning("请录入编码！"); return; }

          this.loadingStart = true;
          const postData = {
            modelCode: selectedModelCode,
            deviceSn: this.startForm.deviceSn,
            workerId: this.startForm.workerId
          };

          // 1. 创建/获取总任务
          startTask(postData).then(res => {
            this.taskInfo = res.data;
            // 补全可能缺失的数据
            if (!this.taskInfo.modelCode) this.taskInfo.modelCode = selectedModelCode;
            this.taskInfo.deviceType = selectedModelCode;
            this.taskInfo.workerId = this.startForm.workerId;
            this.sessionStartTime = new Date();

            // 2. 获取 SOP 步骤列表
            this.fetchSopSteps(selectedModelCode);

          }).catch(err => {
            console.error("❌ [前端] 任务创建失败:", err);
            this.loadingStart = false;
          });
        },

        fetchSopSteps(currentModelCode) {
          listStep({modelCode: currentModelCode}).then(res => {
            if (!res.rows || res.rows.length === 0) {
               this.$message.warning("该型号没有配置 SOP 步骤！");
               this.loadingStart = false;
               return;
            }

            this.stepList = res.rows.sort((a, b) => a.stepOrder - b.stepOrder);
            this.isWorking = true;
            this.isFinished = false;
            this.loadingStart = false;
            this.openDialog = false;

            // 恢复进度逻辑
            if (this.taskInfo.currentStepIndex && this.taskInfo.currentStepIndex > 1) {
              this.currentStepIndex = this.taskInfo.currentStepIndex - 1;
              this.$notify({title: '恢复作业', message: `已恢复至第 ${this.taskInfo.currentStepIndex} 步`, type: 'success'});
            } else {
              this.currentStepIndex = 0;
              this.$message.success("开工成功，AI 引擎已就绪");
            }

            // 🔥 核心：界面渲染完成后，立即调用后端开始录像
            // 此时 triggerStepStart 会带上 modelCode，确保 Python 正常工作
            this.$nextTick(() => {
               this.triggerStepStart();
               this.startSync(); // 恢复轮询
            });

          }).catch(err => {
            this.loadingStart = false;
            this.openDialog = false;
            console.error(err);
          });
        },

        // 【上一步】
        handlePrevStep() {
          if (this.currentStepIndex <= 0 || this.loadingStep) return;
          this.stopSync();
          this.loadingStep = true;

          // 1. 停止当前步 (传入 '1'，表示步骤中断/结束，但整机未完)
          this.triggerStepStop('1').then(() => {
            const prevIndex = this.currentStepIndex - 1;

            // 更新任务表状态
            updateTask({
              taskId: this.taskInfo.taskId,
              currentStepIndex: prevIndex + 1,
              status: '0' // 任务状态保持进行中
            }).then(() => {
              this.currentStepIndex = prevIndex;
              this.$message.info(`已返回上一步`);

              // 重新开始上一步的录像
              this.triggerStepStart();

            }).finally(() => {
              this.loadingStep = false;
              this.startSync();
            });
          });
        },

        // 【下一步】
        handleNextStep() {
          if (this.loadingStep) return;
          this.stopSync();
          this.loadingStep = true;

          // 1. 停止当前步 (传入 '1'，表示步骤结束，整机状态仍为进行中)
          this.triggerStepStop('1').then(() => {

            // 2. 更新任务指针
            const nextIndex = this.currentStepIndex + 1;
            const isLastStep = nextIndex >= this.stepList.length;

            updateTask({
              taskId: this.taskInfo.taskId,
              status: isLastStep ? '2' : '0', // 只有到最后一步才改任务状态为完成
              currentStepIndex: nextIndex + 1
            }).then(() => {

              if (isLastStep) {
                this.finishWorkLogic();
              } else {
                this.currentStepIndex++; // UI 指针后移
                this.$message.success("步骤完成");

                // 3. 开启下一步 (后端会创建状态为 1 的新日志)
                this.$nextTick(() => {
                    this.triggerStepStart();
                });
              }
            }).finally(() => {
              this.loadingStep = false;
              this.startSync();
            });
          });
        },

        // 完工逻辑拆分 (纯 UI 逻辑，无需改动)
        finishWorkLogic() {
          this.isFinished = true;
          this.isWorking = false;
          this.stopSync();
          this.$message.success("所有步骤已完成，请点击【归档】入库！");
        },

        // 【归档】
            finishWork() {
              // 构造归档日志请求
              const payload = {
                deviceSn: this.taskInfo.deviceSn,
                stepId: 999, // 特殊ID，标识归档步骤
                stepName: '归档入库',
                status: '2', //
                assemblyRound: this.taskInfo.assemblyRound,
                modelCode: this.taskInfo.modelCode,
                workerName: this.taskInfo.workerName
              };

              // 调用 start 接口写入一条“已完成”的记录
              startSopStep(payload).then(() => {
                const roundText = this.taskInfo.assemblyRound > 1 ? `第${this.taskInfo.assemblyRound}次` : '首次';
                this.$message.success(`设备已入库，全流程结束！`);

                this.resetWorkbench();
                this.openDialog = true;
                setTimeout(() => {
                  if (this.$refs.snInput) this.$refs.snInput.focus();
                }, 200);
              });
            },

        // 【暂停】
        handlePause() {
          this.$confirm('确定要暂停当前任务吗？', '提示', {type: 'warning'}).then(() => {
            // 🔥 关键：传入 '3'，告知后端这是暂停
            this.triggerStepStop('3').then(() => {
               // 更新任务表状态为 1 (暂停)
               updateTask({taskId: this.taskInfo.taskId, status: '1'}).then(() => {
                // ❌ 已删除 handleSaveLog
                this.$message.warning("任务已暂停");
                this.resetWorkbench();
                this.stopSync();
                this.openDialog = true;
              });
            });
          });
        },

        // 【取消/关闭】(保持原样)
        handleCancel() {
          this.openDialog = false;
          this.$store.dispatch('tagsView/delView', this.$route).then(() => {
            this.$router.push('/');
          });
        },

        // (保持原样)
        resetWorkbench() {
          this.isWorking = false;
          this.isFinished = false;
          this.stepList = [];
          this.taskInfo = {taskId: null, deviceSn: '', deviceType: '', workerName: '', status: '0'};
          this.startForm.deviceSn = '';
          this.currentLogId = null;
          this.setRecordingUI(false);
        },

    // ---------------------------------------------------------------
    // 📝 日志与轮询 (恢复了您原本的逻辑)
    // ---------------------------------------------------------------

    handleSaveLog(status, remark, videoUrl = null, overrideStartTime = null) {
      if (!this.sessionStartTime && !overrideStartTime) return;

      const finalStartTime = overrideStartTime || this.sessionStartTime;
      // status=2(完工)时不记录 stepId
      const stepIdToSave = (status === '2') ? null : (this.currentStep ? this.currentStep.stepId : null);

      const logData = {
        serialNumber: this.taskInfo.deviceSn,
        modelCode: this.taskInfo.modelCode || this.startForm.deviceType,
        workerName: this.taskInfo.workerName, // 假设后台返回了这个字段，或者通过 ID 查找
        processStage: remark,
        stepId: stepIdToSave,
        assemblyRound: this.taskInfo.assemblyRound || 1,
        videoUrl: videoUrl,

        // 格式化时间
        startTime: this.parseTime(finalStartTime),
        endTime: this.parseTime(new Date()),

        status: status,
        remark: remark
      };

      addLog(logData).then(() => {
        if (status === '0') {
          // 如果是步骤切换，更新下一段的开始时间
          this.sessionStartTime = new Date();
        }
      });
    },

    startSync() {
      this.stopSync();
      // 1.5秒轮询一次
      this.syncTimer = setInterval(() => { this.checkTaskProgress(); }, 1500);
    },
    stopSync() {
      if (this.syncTimer) { clearInterval(this.syncTimer); this.syncTimer = null; }
    },

    // 轮询检测任务进度 (AI 自动跳步的核心逻辑)
        checkTaskProgress() {
          // 1. 基础拦截：如果没开工、已完工、或无设备号，直接跳过，减少请求
          if (!this.isWorking || this.isFinished || !this.taskInfo || !this.taskInfo.deviceSn) return;

          // 2. 构造查询参数 (只查当前设备的最新任务)
          const queryParams = {
            deviceSn: this.taskInfo.deviceSn,
            pageNum: 1,
            pageSize: 1,
            orderByColumn: 'createTime',
            isAsc: 'desc'
          };

          // 3. 发起查询
          listTask(queryParams).then(res => {
            // 安全校验：防止空数据
            if (!res.rows || res.rows.length === 0) return;

            const remoteTask = res.rows[0];

            // 校验：确保查到的是当前正在做的任务 ID (防止查到旧任务)
            if (remoteTask.taskId !== this.taskInfo.taskId) return;

            // 4. 计算远程进度
            // 数据库存的是步骤号(1, 2, 3...)，前端数组索引是(0, 1, 2...)，所以要减 1
            const backendStep = Number(remoteTask.currentStepIndex || 1);
            const remoteIndex = backendStep - 1;

            // 5. 🔥 [核心] 如果远程进度 > 本地进度，说明 AI 已经自动过站
            if (remoteIndex > this.currentStepIndex) {
              console.log(`📡 [自动流转] 检测到进度更新: Step ${this.currentStepIndex + 1} -> ${backendStep}`);

              // === 同步本地状态 ===
              this.currentStepIndex = remoteIndex;

              // === 分支处理：完工 还是 下一步 ===
              if (remoteTask.status === '2') {
                // [情况A] 任务已全部完工
                this.finishWorkLogic(); // 调用完工结算逻辑

              } else {
                // [情况B] 只是跳到了下一步

                // ⚠️ 关键：因为是 AI 自动跳的，前端此时手里拿的是上一步的 LogID。
                // 必须先清空，防止用户此时点击“暂停”导致操作了上一步的记录。
                this.currentLogId = null;

                // 视觉反馈：保持红灯亮起
                this.setRecordingUI(true);

                this.$notify({
                  title: 'AI 自动流转',
                  message: `步骤已自动跳转至第 ${backendStep} 步`,
                  type: 'success',
                  duration: 2500
                });

                // 🔥 [最关键一步] 自动触发前端的“开始”逻辑
                // 作用：调用后端 startSopStep -> 获取当前新步骤的 LogID -> 赋值给 this.currentLogId
                // 这样用户后续点击“暂停”或“上一步”时，才有正确的 ID 可用。
                this.$nextTick(() => {
                    this.triggerStepStart();
                });
              }
            }
          }).catch(err => {
            // 捕获轮询中的网络波动，防止控制台报红，静默处理即可
            // console.warn("轮询异常:", err);
          });
        },}
}
</script>

<style scoped lang="scss">
/* 1. 全局背景 */
.workbench-container {
  background-color: #f5f7fa;
  height: calc(100vh - 84px);
  padding: 15px;
}

/* 2. 顶部栏布局 */
.top-bar {
  background: #ffffff;
  padding: 15px 30px;
  border-radius: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  margin-bottom: 20px;

  .bar-section {
    display: flex;
    flex-direction: column;
    justify-content: center;

    .label { font-size: 13px; color: #909399; margin-bottom: 4px; }

    &.left .main-title { font-size: 20px; font-weight: bold; color: #303133; }

    &.center {
      align-items: center;
      flex-direction: row;
      .label { margin-right: 10px; margin-bottom: 0; }
      .sn-text { font-size: 24px; font-weight: 800; color: #409EFF; letter-spacing: 1px; }
    }

    &.right { align-items: flex-end; }
  }
}

/* ✨ [新增] 录像红点样式 ✨ */
.recording-status {
  display: inline-flex;
  align-items: center;
  margin-left: 20px;
  background: #2b2b2b; /* 深色背景 */
  color: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-family: monospace; /* 等宽字体显示时间 */
  border: 1px solid #555;

  .red-dot {
    width: 10px;
    height: 10px;
    background-color: #ff4d4f;
    border-radius: 50%;
    margin-right: 8px;
    animation: blink 1s infinite ease-in-out; /* 呼吸灯动画 */
  }
}

@keyframes blink {
  0% { opacity: 1; transform: scale(1); box-shadow: 0 0 0 rgba(255, 77, 79, 0); }
  50% { opacity: 0.6; transform: scale(0.8); box-shadow: 0 0 5px rgba(255, 77, 79, 0.5); }
  100% { opacity: 1; transform: scale(1); box-shadow: 0 0 0 rgba(255, 77, 79, 0); }
}

.big-pause-btn {
  padding: 12px 25px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 1px;
  box-shadow: 0 4px 10px rgba(245, 108, 108, 0.3);
}

.clean-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}

.visual-card, .sop-card {
  background-color: #ffffff;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  border-radius: 6px;
}

/* 流程条样式 */
.process-flow-bar {
  background: #ffffff;
  padding: 25px 20px;
  border-top: 1px solid #ebeef5;
  overflow-x: auto;

  .custom-steps {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
  }

  .step-item {
    display: flex;
    align-items: center;

    .step-capsule {
      padding: 6px 18px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: bold;
      white-space: nowrap;
      transition: all 0.3s;
      border: 1px solid transparent;
      display: flex;
      align-items: center;
    }

    .step-line {
      width: 60px;
      height: 2px;
      background: #e4e7ed;
      margin: 0 10px;
    }

    &.is-wait .step-capsule { background: #f4f4f5; color: #909399; border-color: #e9e9eb; }

    &.is-current {
      .step-capsule {
        background: #409EFF;
        color: white;
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
        transform: scale(1.05);
      }
      .step-line { background: #e4e7ed; }
    }

    &.is-finished {
      .step-capsule { background: #f0f9eb; color: #67C23A; border-color: #c2e7b0; }
      .step-line { background: #67C23A; }
    }
  }
}

/* ✨ [重构] SOP 区域样式：布局优化 ✨ */
.sop-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 10px;

  .target-bar {
    margin-bottom: 15px;
    font-size: 16px;
    color: #606266;
    background: #ecf5ff;
    padding: 10px 15px;
    border-radius: 6px;
    border-left: 5px solid #409EFF;

    .target-highlight {
      font-weight: bold;
      color: #E6A23C;
      font-size: 18px;
      margin-left: 5px;
    }
  }

  .sop-desc.full-height {
    flex: 1; /* 占满剩余空间 */
    background: #fdf6ec; /* 浅黄护眼背景 */
    padding: 20px;
    border-radius: 8px;
    border: 1px solid #faecd8;
    overflow-y: auto;

    h4 {
      margin-top: 0;
      font-size: 18px;
      color: #d48806;
      border-bottom: 1px dashed #d48806;
      padding-bottom: 10px;
      margin-bottom: 15px;
    }

    .desc-text {
      font-size: 20px; /* 大字号 */
      line-height: 1.8;
      color: #303133;
      font-weight: 500;
      white-space: pre-wrap;
    }
  }

  /* 底部按钮区布局 */
  .action-area {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    gap: 15px;

    .prev-btn {
      width: 120px;
      height: 50px;
      font-size: 16px;
    }

    .next-btn {
      flex: 1;
      height: 50px;
      font-size: 18px;
      font-weight: bold;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    }
  }
}

/* 空状态和完成状态保持原样 */
.empty-state, .finish-state {
  height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center;
}

::v-deep .start-dialog { border-radius: 6px; .el-dialog__header { border-bottom: 1px solid #f0f0f0; } }
</style>
