package com.kxmall.market.data.domain;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("kxmall_goods_in_stock")
public class GoodsInStockDO {

    //出库id
    private Long id;

    //仓库id
    private Long storageId;

    //出库单号
    private String inStockNumbers;

    //出库状态
    private Integer states;

    //出库人
    private String ingoingPerson;

    //出库时间
    private Date ingoingTime;

    //更新人
    private String updatePerson;

    //更新创建时间
    private Date gmtUpdate;

    //备注
    private String remarks;
}
