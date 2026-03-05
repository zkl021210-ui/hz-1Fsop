<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span style="font-weight: bold; font-size: 18px">🛠️ 101号工位 - 实时状态</span>
            <el-tag style="float: right" type="info" v-if="!isOnline">离线</el-tag>
            <el-tag style="float: right" type="success" v-else>在线运行中</el-tag>
          </div>

          <div class="monitor-box" :class="{ 'error-bg': isError, 'ok-bg': !isError && isOnline, 'offline-bg': !isOnline }">

            <div v-if="!isOnline" style="padding-top: 50px;">
              <i class="el-icon-switch-button" style="font-size: 60px; color: #909399;"></i>
              <h3>等待 Python 眼睛启动...</h3>
            </div>

            <div v-else>
              <h2>当前视野检测到：</h2>
              <div class="tags-area">
                <el-tag v-for="(item, index) in currentObjects" :key="index" effect="dark" size="medium" style="margin:5px; font-size: 20px;">
                  {{ item }}
                </el-tag>
                <span v-if="currentObjects.length === 0" style="color: #999">（画面空闲）</span>
              </div>

              <el-divider></el-divider>

              <h3>工序判定结果：</h3>
              <div v-if="isError">
                <i class="el-icon-warning" style="font-size: 60px; color: red;"></i>
                <h1 style="color: red; font-size: 30px; margin: 10px 0;">🚫 违规操作!</h1>
                <p>请立即检查工序顺序</p>
              </div>

              <div v-else>
                <i class="el-icon-success" style="font-size: 60px; color: #67C23A;"></i>
                <h1 style="color: #67C23A; font-size: 30px; margin: 10px 0;">✅ 正常运行</h1>
              </div>
            </div>

          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <div slot="header">🎥 实时监控画面</div>
          <div style="height: 400px; background: #000; color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center;">
            <i class="el-icon-video-camera" style="font-size: 40px; margin-bottom: 20px;"></i>
            <span>视频信号接入中...</span>
            <span style="font-size: 12px; color: #666; margin-top: 10px;">(目前请查看 Python 端的弹窗)</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
// 引入若依封装好的请求工具，用来发 HTTP 请求
import request from '@/utils/request'

export default {
  name: "Monitor",
  data() {
    return {
      timer: null,      // 定时器
      isOnline: false,  // Python 是否在线
      currentObjects: [], // 检测到的物体列表
      isError: false    // 是否违规
    };
  },
  created() {
    // 页面一打开，就开始轮询
    this.startPolling();
  },
  beforeDestroy() {
    // 页面关闭时，记得关掉定时器，不然浏览器会卡
    clearInterval(this.timer);
  },
  methods: {
    startPolling() {
      // 每 1000 毫秒 (1秒) 问一次 Java 后端
      this.timer = setInterval(() => {
        this.getLiveStatus();
      }, 1000);
    },
    getLiveStatus() {
      // 发送 GET 请求给 Java Controller
      // 这里的路径必须和你 Java 写的 @RequestMapping 一致
      request({
        url: '/system/process/live/101',
        method: 'get'
      }).then(response => {
        // 如果成功拿到数据
        this.isOnline = true;
        const data = response.data; // 注意：若依的 AjaxResult 数据在 data 字段里，或者直接是 response

        // 更新页面数据
        if (data) {
          this.currentObjects = data.objects || [];
          this.isError = data.error || false;
        }
      }).catch(() => {
        // 如果报错（比如 Java 没拿到数据），说明 Python 没开
        this.isOnline = false;
      });
    }
  }
};
</script>

<style scoped>
.monitor-box {
  padding: 20px;
  text-align: center;
  border-radius: 8px;
  min-height: 400px;
  transition: all 0.3s;
}
.offline-bg { background-color: #f4f4f5; border: 2px dashed #909399; }
.ok-bg { background-color: #f0f9eb; border: 2px solid #67c23a; }
/* 错误时会有红色闪烁动画 */
.error-bg {
  background-color: #fef0f0;
  border: 2px solid #f56c6c;
  animation: blink-red 1s infinite;
}

@keyframes blink-red {
  0% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(245, 108, 108, 0); }
  100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0); }
}
</style>
