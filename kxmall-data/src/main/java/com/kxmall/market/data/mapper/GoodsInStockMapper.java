package com.kxmall.market.data.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.kxmall.market.data.domain.GoodsInStockDO;

public interface GoodsInStockMapper extends BaseMapper<GoodsInStockDO> {

    /**
     * 出库商品数更新
     *
     * @return
     */
    GoodsInStockDO selectByMax();
}
