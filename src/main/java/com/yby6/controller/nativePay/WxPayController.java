// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.controller.nativePay;

import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.yby6.config.R;
import com.yby6.domain.weChart.tradebill.tradeBillResponse;
import com.yby6.service.WxPayService;
import com.yby6.util.GsonUtils;
import com.yby6.util.HttpUtils;
import com.yby6.util.WechatPay2ValidatorForRequest;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * ../网站微信支付APIv3
 *
 * @author Yang Shuai
 * Create By 2023/05/21
 */
@CrossOrigin
@RestController
@RequestMapping("/api/wx-pay")
@Slf4j
public class WxPayController {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private Verifier verifier;

    @ApiOperation("调用统一下单API，生成支付二维码")
    @PostMapping("/native/{productId}")
    public R nativePay(@PathVariable Long productId) throws Exception {
        log.info("发起支付请求 v3, 返回支付二维码连接和订单号");
        return R.ok().setData(wxPayService.nativePay(productId));
    }

    @ApiOperation("支付通知->微信支付通过支付通知接口将用户支付成功消息通知给商户")
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
        log.info("接收到微信服务回调......");
        Map<String, String> map = new HashMap<>();// 应答对象，给微信服务答复是否通知成功

        try {
            //处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = GsonUtils.toObject(body, HashMap.class);
            String requestId = (String) bodyMap.get("id");
            log.info("支付通知的id ===> {}", requestId);

            //签名的验证
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest = new WechatPay2ValidatorForRequest(verifier, requestId, body);
            if (!wechatPay2ValidatorForRequest.validate(request)) {
                log.error("通知验签失败");
                //失败应答
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return GsonUtils.toJsonStr(map);
            }
            log.info("通知验签成功");

            // 处理订单
            wxPayService.processOrder(bodyMap);
            log.info("回调业务处理完毕");
            // 成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return GsonUtils.toJsonStr(map);
        } catch (Exception e) {
            e.printStackTrace();
            //失败应答
            response.setStatus(500);
            map.put("code", "FAIL");
            map.put("message", "失败");
            return GsonUtils.toJsonStr(map);
        }

    }

    /**
     * 用户取消订单
     */
    @ApiOperation("用户取消订单")
    @PostMapping("/cancel/{orderNo}")
    public R cancel(@PathVariable String orderNo) throws Exception {
        log.info("取消订单");
        wxPayService.cancelOrder(orderNo);
        return R.ok().setMessage("订单已取消");
    }

    /**
     * 查询订单
     *
     * @param orderNo
     * @return
     * @throws Exception
     */
    @ApiOperation("查询订单：测试订单状态用")
    @GetMapping("/query/{orderNo}")
    public R queryOrder(@PathVariable String orderNo) throws Exception {

        log.info("查询订单");

        String result = wxPayService.queryOrder(orderNo);
        return R.ok().setMessage("查询成功").data("result", result);

    }

    @ApiOperation("申请退款")
    @PostMapping("/refunds/{orderNo}/{refundsNo}/{reason}")
    public R refunds(@PathVariable String orderNo, @PathVariable String refundsNo, @PathVariable String reason) throws Exception {
        log.info("申请退款");
        wxPayService.refund(orderNo, reason, refundsNo);
        return R.ok();
    }

    /**
     * 查询退款
     *
     * @param refundNo
     * @return
     * @throws Exception
     */
    @ApiOperation("查询退款：测试用")
    @GetMapping("/query-refund/{refundNo}")
    public R queryRefund(@PathVariable String refundNo) throws Exception {

        log.info("查询退款");

        String result = wxPayService.queryRefund(refundNo);
        return R.ok().setMessage("查询成功").data("result", result);
    }


    /**
     * 退款结果通知
     * 退款状态改变后，微信会把相关退款结果发送给商户。
     */
    @ApiOperation("退款结果通知")
    @PostMapping("/refunds/notify")
    public String refundsNotify(HttpServletRequest request, HttpServletResponse response) {

        log.info("退款通知执行");

        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();// 应答对象

        try {
            //处理通知参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            log.info("退款参数：{}", bodyMap);
            String requestId = (String) bodyMap.get("id");
            log.info("支付通知的id ===> {}", requestId);

            //签名的验证
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest = new WechatPay2ValidatorForRequest(verifier, requestId, body);
            if (!wechatPay2ValidatorForRequest.validate(request)) {

                log.error("通知验签失败");
                //失败应答
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
            log.info("通知验签成功");

            // 处理退款单
            wxPayService.processRefund(bodyMap);

            // 成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return gson.toJson(map);
        } catch (Exception e) {
            e.printStackTrace();
            //失败应答
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "失败");
            return gson.toJson(map);
        }
    }

    /**
     * 申请交易账单API
     *
     * @param billDate 账单日期 格式yyyy-MM-dd 仅支持三个月内的账单下载申请
     * @param type     账单类型 不填则默认是ALL
     *                 ALL：返回当日所有订单信息（不含充值退款订单）
     *                 SUCCESS：返回当日成功支付的订单（不含充值退款订单）
     *                 REFUND：返回当日退款订单（不含充值退款订单）
     */
    @ApiOperation("获取账单url：测试用")
    @GetMapping("/querybill/{billDate}/{type}")
    public R queryTradeBill(@PathVariable String billDate, @PathVariable String type) throws Exception {

        log.info("获取账单url");

        tradeBillResponse tradeBillResponse = wxPayService.queryBill(billDate, type);

        return R.ok().setMessage("获取账单url成功").data("downloadUrl", tradeBillResponse.getDownload_url()).data("tradeBillResponse", tradeBillResponse);
    }

    @ApiOperation("下载账单")
    @GetMapping("/downloadbill/{billDate}/{type}")
    public R downloadBill(@PathVariable String billDate, @PathVariable String type) throws Exception {

        log.info("下载账单");
        String result = wxPayService.downloadBill(billDate, type);

        return R.ok().data("result", result);
    }

}
