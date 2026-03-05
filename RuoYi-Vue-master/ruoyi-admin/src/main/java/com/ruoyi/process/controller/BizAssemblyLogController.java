package com.ruoyi.process.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.process.domain.BizAssemblyLog;
import com.ruoyi.process.service.IBizAssemblyLogService;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.sql.SqlUtil;
import com.ruoyi.common.utils.StringUtils;
import com.github.pagehelper.PageHelper;

@RestController
@RequestMapping("/process/assemblyLog") // 这里的路径和前端要对应
public class BizAssemblyLogController extends BaseController
{
    @Autowired
    private IBizAssemblyLogService bizAssemblyLogService;

    @GetMapping("/list")
    public TableDataInfo list(BizAssemblyLog bizAssemblyLog)
    {

        PageDomain pageDomain = TableSupport.buildPageRequest();

        pageDomain.setOrderByColumn("create_time");
        pageDomain.setIsAsc("desc");

        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        PageHelper.startPage(pageNum, pageSize, orderBy);

        List<BizAssemblyLog> list = bizAssemblyLogService.selectBizAssemblyLogList(bizAssemblyLog);
        return getDataTable(list);
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response, BizAssemblyLog bizAssemblyLog)
    {
        List<BizAssemblyLog> list = bizAssemblyLogService.selectBizAssemblyLogList(bizAssemblyLog);
        ExcelUtil<BizAssemblyLog> util = new ExcelUtil<BizAssemblyLog>(BizAssemblyLog.class);
        util.exportExcel(response, list, "装配流水数据");
    }

    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return AjaxResult.success(bizAssemblyLogService.selectBizAssemblyLogById(logId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody BizAssemblyLog bizAssemblyLog)
    {
        return toAjax(bizAssemblyLogService.insertBizAssemblyLog(bizAssemblyLog));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody BizAssemblyLog bizAssemblyLog)
    {
        return toAjax(bizAssemblyLogService.updateBizAssemblyLog(bizAssemblyLog));
    }

    @DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable Long[] logIds)
    {
        return toAjax(bizAssemblyLogService.deleteBizAssemblyLogByIds(logIds));
    }
}