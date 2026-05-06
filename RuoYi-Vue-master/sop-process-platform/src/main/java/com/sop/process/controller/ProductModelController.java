package com.sop.process.controller;

import com.sop.common.page.PageResult;
import com.sop.common.result.ApiResult;
import com.sop.process.dto.ProductModelDTO;
import com.sop.process.service.ProductModelService;
import com.sop.process.vo.ProductModelVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "产品型号管理")
@RestController
@RequestMapping("/api/process/product-model")
public class ProductModelController {

    private final ProductModelService productModelService;

    public ProductModelController(ProductModelService productModelService) {
        this.productModelService = productModelService;
    }

    @Operation(summary = "分页查询产品型号")
    @GetMapping("/page")
    public ApiResult<PageResult<ProductModelVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String modelName) {
        return ApiResult.success(PageResult.from(page, pageSize,
                productModelService.pageProductModels(page, pageSize, modelCode, modelName)));
    }

    @Operation(summary = "查询产品型号详情")
    @GetMapping("/{modelId}")
    public ApiResult<ProductModelVO> getById(@PathVariable Long modelId) {
        return ApiResult.success(productModelService.getProductModelById(modelId));
    }

    @Operation(summary = "新增产品型号")
    @PostMapping
    public ApiResult<ProductModelVO> create(@RequestBody ProductModelDTO dto) {
        return ApiResult.success(productModelService.createProductModel(dto));
    }

    @Operation(summary = "修改产品型号")
    @PutMapping("/{modelId}")
    public ApiResult<ProductModelVO> update(@PathVariable Long modelId, @RequestBody ProductModelDTO dto) {
        return ApiResult.success(productModelService.updateProductModel(modelId, dto));
    }

    @Operation(summary = "删除产品型号")
    @DeleteMapping("/{modelId}")
    public ApiResult<Void> delete(@PathVariable Long modelId) {
        productModelService.deleteProductModel(modelId);
        return ApiResult.success();
    }
}
