<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover" class="welcome-card">
          <div slot="header" class="clearfix header-flex">
            <div class="header-left">
              <span class="system-title"><i class="el-icon-monitor"></i> 转辙机智能装配监控中心</span>
              <el-tag type="success" effect="dark" class="status-tag">产线运行中</el-tag>
            </div>
            <div class="header-right">
              <span class="env-item"><i class="el-icon-time"></i> {{ currentTime }}</span>
            </div>
          </div>
          <div class="welcome-content">
            <div class="info">
              <h3>你好，{{ user.nickName }} (工号: {{ user.userName }})</h3>
              <p>欢迎回到智能作业系统，请通过左侧菜单栏进入功能模块。</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card shadow="hover" class="data-card">
          <div class="stat-item">
            <div class="stat-icon icon-blue"><i class="el-icon-finished"></i></div>
            <div class="stat-info">
              <div class="stat-title">今日产量 (台)</div>
              <div class="stat-value" v-loading="loading">{{ summaryData.finishedToday }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="data-card">
            <div class="stat-item">
              <div class="stat-icon icon-green"><i class="el-icon-s-data"></i></div>
              <div class="stat-info">
                <div class="stat-title">过往七日产量 (台)</div>
                <div class="stat-value" v-loading="loading">{{ summaryData.finishedLast7Days }}</div>
              </div>
            </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="data-card">
          <div class="stat-item">
            <div class="stat-icon icon-red"><i class="el-icon-warning"></i></div>
            <div class="stat-info">
              <div class="stat-title">今日异常 (起)</div>
              <div class="stat-value" v-loading="loading">{{ summaryData.errorsToday }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <div slot="header">
            <span><i class="el-icon-bell"></i> 任务日志 (进行中/暂停)</span>
          </div>
          <div class="log-list" style="height: 320px; overflow-y: auto;" v-loading="loading">
             <div v-if="realtimeLogs.length === 0" style="text-align: center; color: #999; padding: 20px 0;">暂无相关日志记录</div>
            <ul style="list-style: none; padding: 0; margin: 0;">
              <li v-for="log in realtimeLogs" :key="log.logId" class="log-item">
                <div class="log-time">{{ parseTime(log.startTime, '{m}-{d} {h}:{i}:{s}') }}</div>
                <div class="log-content">
                  <span class="log-user">{{ log.workerName }}</span>
                  <span class="log-action">{{ log.processStage }}</span>
                  <span class="log-sn">{{ log.serialNumber }}</span>
                </div>
                <el-tag size="mini" :type="log.status === '3' ? 'warning' : 'info'">
                  {{ log.status === '1' ? '进行中' : (log.status === '3' ? '暂停' : '其他') }}
                </el-tag>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import { listTask } from "@/api/process/task";
import { listLog } from "@/api/process/log";
import { parseTime } from "@/utils/ruoyi";

export default {
  name: "Index",
  data() {
    return {
      currentTime: "",
      clockTimer: null,
      dataTimer: null,
      loading: true,
      summaryData: {
        finishedToday: 0,
        finishedLast7Days: 0,
        errorsToday: 0,
      },
      realtimeLogs: [],
    };
  },
  computed: {
    ...mapGetters(['name', 'userName']),
    user() {
      return { nickName: this.name, userName: this.userName }
    }
  },
  created() {
    this.currentTime = this.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}');
  },
  mounted() {
    this.clockTimer = setInterval(() => {
      this.currentTime = this.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}');
    }, 1000);

    this.fetchDashboardData();
    this.dataTimer = setInterval(this.fetchDashboardData, 10000);
  },
  beforeDestroy() {
    clearInterval(this.clockTimer);
    clearInterval(this.dataTimer);
  },
  methods: {
    parseTime,
    fetchDashboardData() {
      this.loading = true;

      const todayStart = new Date(new Date().setHours(0, 0, 0, 0));
      const todayEnd = new Date(new Date().setHours(23, 59, 59, 999));
      const sevenDaysAgo = new Date();
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
      const sevenDaysAgoStart = new Date(sevenDaysAgo.setHours(0, 0, 0, 0));

      const finishedTodayPromise = listTask({
        pageNum: 1,
        pageSize: 1,
        status: '2',
        params: {
          beginTime: parseTime(todayStart),
          endTime: parseTime(todayEnd)
        }
      });

      const finishedLast7DaysPromise = listTask({
        pageNum: 1,
        pageSize: 1,
        status: '2',
        params: {
          beginTime: parseTime(sevenDaysAgoStart),
          endTime: parseTime(todayEnd)
        }
      });

      const errorsTodayPromise = listLog({
        pageNum: 1,
        pageSize: 1,
        status: '3',
        params: {
          beginTime: parseTime(todayStart),
          endTime: parseTime(todayEnd)
        }
      });

      const historicalLogPromise = listLog({
        pageNum: 1,
        pageSize: 100,
        status: '1,3',
        orderByColumn: 'startTime',
        isAsc: 'desc',
      });

      Promise.all([finishedTodayPromise, finishedLast7DaysPromise, errorsTodayPromise, historicalLogPromise]).then(([finishedTodayRes, finishedLast7DaysRes, errorsTodayRes, historicalLogRes]) => {
        this.summaryData.finishedToday = finishedTodayRes.total;
        this.summaryData.finishedLast7Days = finishedLast7DaysRes.total;
        this.summaryData.errorsToday = errorsTodayRes.total;
        this.realtimeLogs = historicalLogRes.rows;

        this.loading = false;
      }).catch(err => {
        console.error("获取首页数据失败:", err);
        this.loading = false;
      });
    }
  }
};
</script>

<style scoped lang="scss">
.home {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.system-title { font-size: 20px; font-weight: bold; color: #303133; margin-right: 15px; }
.status-tag { transform: translateY(-2px); }
.env-item { margin-left: 20px; color: #606266; font-size: 14px; b { color: #303133; } }

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;

  .info {
    h3 { margin: 0 0 5px 0; font-size: 18px; color: #303133; }
    p { margin: 5px 0; color: #909399; font-size: 13px; }
  }
}

.stat-item {
  display: flex;
  align-items: center;
  .stat-icon {
    width: 50px; height: 50px; border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    font-size: 24px; color: #fff; margin-right: 15px;
  }
  .icon-blue { background: linear-gradient(135deg, #36D1DC, #5B86E5); }
  .icon-green { background: linear-gradient(135deg, #11998e, #38ef7d); }
  .icon-red { background: linear-gradient(135deg, #ff9966, #ff5e62); }

  .stat-info {
    .stat-title { font-size: 13px; color: #909399; }
    .stat-value { font-size: 22px; font-weight: bold; color: #303133; }
  }
}

.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
  font-size: 13px;

  &:last-child { border-bottom: none; }

  .log-time { color: #909399; width: 120px; }
  .log-content {
    flex: 1; margin: 0 10px;
    .log-user { font-weight: bold; color: #303133; margin-right: 5px; }
    .log-action { margin-right: 10px; }
    .log-sn { color: #409EFF; background: #ecf5ff; padding: 0 4px; border-radius: 4px; font-size: 12px; }
  }
}
</style>
