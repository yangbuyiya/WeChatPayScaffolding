// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

@TableName(value = "t_product")
public class TProduct implements Serializable {
    /**
     * 商Bid
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 商品名称
     */
    @TableField(value = "title")
    private String title;

    /**
     * 价格(分)
     */
    @TableField(value = "price")
    private Integer price;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    /**
     * 获取商Bid
     *
     * @return id - 商Bid
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置商Bid
     *
     * @param id 商Bid
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取商品名称
     *
     * @return title - 商品名称
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置商品名称
     *
     * @param title 商品名称
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取价格(分)
     *
     * @return price - 价格(分)
     */
    public Integer getPrice() {
        return price;
    }

    /**
     * 设置价格(分)
     *
     * @param price 价格(分)
     */
    public void setPrice(Integer price) {
        this.price = price;
    }

    /**
     * 获取创建时间
     *
     * @return create_time - 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取更新时间
     *
     * @return update_time - 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}