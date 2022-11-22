package com.kxmall.market.admin.api.api.goodsinstock;

import com.kxmall.market.core.annotation.HttpMethod;
import com.kxmall.market.core.annotation.HttpOpenApi;
import com.kxmall.market.core.annotation.HttpParam;
import com.kxmall.market.core.annotation.HttpParamType;
import com.kxmall.market.core.annotation.param.NotNull;
import com.kxmall.market.core.exception.ServiceException;
import com.kxmall.market.data.domain.GoodsInStockDO;
import com.kxmall.market.data.domain.StorageDO;
import com.kxmall.market.data.dto.GoodsInStockDTO;
import com.kxmall.market.data.model.Page;

import java.util.List;

@HttpOpenApi(group = "admin.goodsInStock",description = "商品入库服务")
public interface GoodsInStockService {
    @HttpMethod(description = "列表", permission = "admin:goodsInStock:list", permissionParentName = "前置仓管理", permissionName = "商品入库")
    public Page<GoodsInStockDO> list(
            @HttpParam(name = "storageId", type = HttpParamType.COMMON, description = "仓库名称") Long storageId,
            @HttpParam(name = "inStockNumbers", type = HttpParamType.COMMON, description = "入库单号") String inStockNumbers,
            @HttpParam(name = "states", type = HttpParamType.COMMON, description = "入库状态") Integer states,
            @HttpParam(name = "ingoingDay", type = HttpParamType.COMMON, description = "入库日期") String ingoingDay,
            @HttpParam(name = "page", type = HttpParamType.COMMON, description = "页码", valueDef = "1") Integer page,
            @HttpParam(name = "limit", type = HttpParamType.COMMON, description = "页码长度", valueDef = "20") Integer limit,
            @HttpParam(name = "adminId", type = HttpParamType.ADMIN_ID, description = "管理员ID") Long adminId) throws ServiceException;

    @HttpMethod(description = "详情", permission = "admin:goodsInStock:selectById", permissionParentName = "前置仓管理", permissionName = "商品入库")
    public GoodsInStockDTO selectById(
            @NotNull @HttpParam(name = "InStockNumbers", type = HttpParamType.COMMON, description = "入库单号") String InStockNumbers,
            @NotNull @HttpParam(name = "id", type = HttpParamType.COMMON, description = "入库id") Long id,
            @HttpParam(name = "adminId", type = HttpParamType.ADMIN_ID, description = "管理员ID") Long adminId) throws ServiceException;

    @HttpMethod(description = "添加", permission = "admin:goodsInStock:create", permissionParentName = "前置仓管理", permissionName = "商品入库")
    public GoodsInStockDTO create(
            @NotNull @HttpParam(name = "goodsInStockDTO", type = HttpParamType.COMMON, description = "商品入库DTO对象") GoodsInStockDTO goodsInStockDTO,
            @HttpParam(name = "adminId", type = HttpParamType.ADMIN_ID, description = "管理员ID") Long adminId) throws ServiceException;

    @HttpMethod(description = "更新", permission = "admin:goodsInStock:update", permissionParentName = "前置仓管理", permissionName = "商品入库")
    public GoodsInStockDTO update(
            @NotNull @HttpParam(name = "goodsInStockDTO", type = HttpParamType.COMMON, description = "商品入库对象") GoodsInStockDTO goodsInStockDTO,
            @HttpParam(name = "adminId", type = HttpParamType.ADMIN_ID, description = "管理员ID") Long adminId) throws ServiceException;

    @HttpMethod(description = "入库", permission = "admin:goodsInStock:updateInOfStock", permissionParentName = "前置仓管理", permissionName = "商品入库")
    public String updateInOfStock(
            @NotNull @HttpParam(name = "ingoingPerson", type = HttpParamType.COMMON, description = "入库人") String ingoingPerson,
            @NotNull @HttpParam(name = "adminId", type = HttpParamType.ADMIN_ID, description = "管理员ID") Long adminId,
            @NotNull @HttpParam(name = "storageId", type = HttpParamType.COMMON, description = "仓库id") Long storageId,
            @NotNull @HttpParam(name = "inStockNumbers", type = HttpParamType.COMMON, description = "入库单号") String inStockNumbers) throws ServiceException;

    @HttpMethod(description = "删除入库信息", permission = "admin:goodsinStock:delete", permissionParentName = "前置仓管理", permissionName = "商品入库")
    public String delete(
            @NotNull @HttpParam(name = "adminId", type = HttpParamType.ADMIN_ID, description = "管理员ID") Long adminId,
            @NotNull @HttpParam(name = "id", type = HttpParamType.COMMON, description = "入库主键id") Long id,
            @NotNull @HttpParam(name = "inStockNumbers", type = HttpParamType.COMMON, description = "入库单号") String inStockNumbers) throws ServiceException;

    @HttpMethod(description = "获取所有仓库的名称", permission = "admin:goodsInStock:storagAllName", permissionParentName = "前置仓管理", permissionName = "商品入库")
    public List<StorageDO> storagAllName(
            @HttpParam(name = "adminId", type = HttpParamType.ADMIN_ID, description = "管理员ID") Long adminId) throws ServiceException;

}
