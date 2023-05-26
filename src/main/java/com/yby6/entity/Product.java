// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_product")
public class Product extends BaseEntity{

    private String title; //商品名称

    private Integer price; //价格（分）
}
