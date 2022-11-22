package com.kxmall.market.data.dto;

import com.kxmall.market.data.domain.GoodsInStockDO;
import com.kxmall.market.data.domain.InStockSpuDO;
import lombok.Data;

import java.util.List;

@Data
public class GoodsInStockDTO extends GoodsInStockDO {
    //出库商品
    private List<InStockSpuDO> inStockSpuDOS;
}
