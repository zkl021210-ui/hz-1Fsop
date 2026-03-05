<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="16" :xs="24">
        <el-card class="box-card" shadow="always">
          <div slot="header" class="clearfix">
            <span><i class="el-icon-camera-solid"></i> 实时工艺监测</span>
            <el-tag type="danger" style="float: right">Live</el-tag>
          </div>
          <div class="video-box">
            <img
              src="http://127.0.0.1:5000/video_feed"
              style="width: 100%; height: 100%; object-fit: contain;"
              alt="等待视频信号..."
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="8" :xs="24">
        <el-card class="box-card" style="margin-bottom: 20px;">
          <div slot="header">
            <span>运行状态</span>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="检测模型">YOLOv8n</el-descriptions-item>
            <el-descriptions-item label="当前工序">
              <el-tag size="small">装配组件</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="设备IP">192.168.1.64</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card>
          <div slot="header"><span>异常日志</span></div>
          <el-timeline>
            <el-timeline-item timestamp="2023/12/23" placement="top" color="#F56C6C">
              <el-card>
                <h4>检测到违规操作</h4>
                <p>工序跳跃：未完成步骤1直接进行步骤2</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "ProcessMonitor",
  data() {
    return {
      // 如果后续需要对接 Java 后端数据，在这里定义变量
    };
  },
  methods: {
    // 这里写具体的方法，比如刷新状态
  }
};
</script>

<style scoped>
.video-box {
  background-color: #000;
  min-height: 500px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
