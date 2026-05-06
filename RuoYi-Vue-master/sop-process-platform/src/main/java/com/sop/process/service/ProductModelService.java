package com.sop.process.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sop.process.domain.ProductModel;
import com.sop.process.dto.ProductModelDTO;
import com.sop.process.vo.ProductModelVO;

import java.util.List;

/**
 * 产品型号 Service 接口
 *
 * @author SOP Team
 */
public interface ProductModelService {

    /**
     * 分页查询产品型号
     */
    Page<ProductModelVO> pageProductModels(int page, int pageSize, String modelCode, String modelName);

    /**
     * 查询所有产品型号
     */
    List<ProductModelVO> listAllProductModels();

    /**
     * 根据ID查询产品型号
     */
    ProductModelVO getProductModelById(Long modelId);

    /**
     * 新增产品型号
     */
    ProductModelVO createProductModel(ProductModelDTO dto);

    /**
     * 修改产品型号
     */
    ProductModelVO updateProductModel(Long modelId, ProductModelDTO dto);

    /**
     * 删除产品型号
     */
    void deleteProductModel(Long modelId);
}
