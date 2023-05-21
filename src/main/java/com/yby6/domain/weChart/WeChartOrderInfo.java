package com.yby6.domain.weChart;

import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.Data;

import java.util.List;

/**
 * <pre>
 *  WeChartOrderInfo
 * </pre>
 *
 * @author 杨不易呀
 * @verison $Id: WeChartOrderInfo v 0.1 2022-11-16 00:43:40
 */
@Data
public class WeChartOrderInfo {

    /**
     * <pre>
     * amount
     * </pre>
     */
    private weChartOrderAmount amount;

    /**
     * <pre>
     *
     * </pre>
     */
    private String appid;

    /**
     * <pre>
     *
     * </pre>
     */
    private String attach;

    /**
     * <pre>
     *
     * </pre>
     */
    private String bank_type;

    /**
     * <pre>
     *
     * </pre>
     */
    private String mchid;

    /**
     * <pre>
     *
     * </pre>
     */
    private String out_trade_no;

    /**
     * <pre>
     * payer
     * </pre>
     */
    private weChartPayer payer;

    /**
     * <pre>
     * promotion_detail
     * </pre>
     */
    private List<String> promotion_detail;

    /**
     * <pre>
     *
     * </pre>
     */
    private String success_time;

    /**
     * <pre>
     *
     * </pre>
     */
    private String trade_state;

    /**
     * <pre>
     * 支付成功
     * </pre>
     */
    private String trade_state_desc;

    /**
     * <pre>
     *
     * </pre>
     */
    private String trade_type;

    /**
     * <pre>
     *
     * </pre>
     */
    private String transaction_id;


}
