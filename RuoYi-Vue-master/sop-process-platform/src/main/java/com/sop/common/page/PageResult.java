package com.sop.common.page;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装
 * <p>
 * 用于统一返回分页查询结果，包含总记录数、页码、每页大小和数据列表。
 *
 * @author SOP Team
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long page;

    /** 每页大小 */
    private long pageSize;

    /** 总页数 */
    private long pages;

    public PageResult() {
        this.records = Collections.emptyList();
        this.total = 0;
        this.page = 1;
        this.pageSize = 10;
        this.pages = 0;
    }

    /**
     * 全参构造方法
     */
    public PageResult(List<T> records, long total, long page, long pageSize) {
        this.records = records != null ? records : Collections.emptyList();
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.pages = pageSize > 0 ? (total + pageSize - 1) / pageSize : 0;
    }

    /**
     * 静态工厂方法 - 从 MP Page 对象创建分页结果
     */
    public static <T> PageResult<T> from(long page, long pageSize, com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> mpPage) {
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal(), page, pageSize);
    }

    /**
     * 静态工厂方法 - 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long total, long page, long pageSize) {
        return new PageResult<>(records, total, page, pageSize);
    }

    /**
     * 静态工厂方法 - 创建空分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>();
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return page > 1;
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return page < pages;
    }
}
