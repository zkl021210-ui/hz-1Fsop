package com.sop.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sop.common.exception.BusinessException;
import com.sop.common.exception.ErrorCode;
import com.sop.process.domain.ProductModel;
import com.sop.process.dto.ProductModelDTO;
import com.sop.process.mapper.ProductModelMapper;
import com.sop.process.service.ProductModelService;
import com.sop.process.vo.ProductModelVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductModelServiceImpl extends ServiceImpl<ProductModelMapper, ProductModel> implements ProductModelService {

    @Override
    public Page<ProductModelVO> pageProductModels(int page, int pageSize, String modelCode, String modelName) {
        Page<ProductModel> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<ProductModel> wrapper = new LambdaQueryWrapper<ProductModel>()
                .eq(ProductModel::getDeleted, 0)
                .like(modelCode != null && !modelCode.isEmpty(), ProductModel::getModelCode, modelCode)
                .like(modelName != null && !modelName.isEmpty(), ProductModel::getModelName, modelName)
                .orderByDesc(ProductModel::getCreateTime);
        Page<ProductModel> result = this.page(pageParam, wrapper);
        Page<ProductModelVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public List<ProductModelVO> listAllProductModels() {
        LambdaQueryWrapper<ProductModel> wrapper = new LambdaQueryWrapper<ProductModel>()
                .eq(ProductModel::getDeleted, 0)
                .orderByAsc(ProductModel::getModelCode);
        return this.list(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public ProductModelVO getProductModelById(Long modelId) {
        ProductModel entity = this.getById(modelId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "产品型号不存在");
        }
        return toVO(entity);
    }

    @Override
    public ProductModelVO createProductModel(ProductModelDTO dto) {
        ProductModel entity = new ProductModel();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);
        return toVO(entity);
    }

    @Override
    public ProductModelVO updateProductModel(Long modelId, ProductModelDTO dto) {
        ProductModel entity = this.getById(modelId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "产品型号不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setModelId(modelId);
        this.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void deleteProductModel(Long modelId) {
        ProductModel entity = this.getById(modelId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "产品型号不存在");
        }
        entity.setDeleted(1);
        this.updateById(entity);
    }

    private ProductModelVO toVO(ProductModel entity) {
        ProductModelVO vo = new ProductModelVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
