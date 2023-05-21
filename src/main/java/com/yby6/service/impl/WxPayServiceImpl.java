package com.yby6.service.impl;

import com.github.wxpay.sdk.WXPayUtil;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.yby6.config.WxPayConfig;
import com.yby6.domain.weChart.WeChartOrderInfo;
import com.yby6.domain.weChart.tradebill.tradeBillResponse;
import com.yby6.entity.OrderInfo;
import com.yby6.entity.PaymentInfo;
import com.yby6.entity.RefundInfo;
import com.yby6.enums.OrderStatus;
import com.yby6.enums.weChatPay.WxRefundStatus;
import com.yby6.enums.weChatPay.WxTradeState;
import com.yby6.enums.weChatPayNative.WxApiType;
import com.yby6.enums.weChatPayNative.WxNotifyType;
import com.yby6.service.OrderInfoService;
import com.yby6.service.PaymentInfoService;
import com.yby6.service.RefundInfoService;
import com.yby6.service.WxPayService;
import com.yby6.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Resource
    private WxPayConfig wxPayConfig;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private RefundInfoService refundsInfoService;

    /**
     * 会进行验证签名
     * 发送请求
     */
    @Resource
    private CloseableHttpClient wxPayClient;

    /**
     * 无需应答签名
     * 无需应答微信只需要我们自己签名
     */
    @Resource
    private CloseableHttpClient wxPayNoSignClient;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 统一调用下单API，生成支付二维码
     * {
     * "mchid": "1900006XXX",
     * "out_trade_no": "native12177525012014070332333",
     * "appid": "wxdace645e0bc2cXXX",
     * "description": "Image形象店-深圳腾大-QQ公仔",
     * "notify_url": "https://weixin.qq.com/",
     * "amount": {
     * "total": 1,
     * "currency": "CNY"
     * }
     * }
     *
     * @param productId 商户ID
     */
    @Override
    public Map<String, Object> nativePay(Long productId) throws Exception {
        log.info("生成订单");
        // 生成订单
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
        String codeUrl = orderInfo.getCodeUrl();
        if (!StringUtils.isEmpty(codeUrl)) {
            log.info("订单已存在，二维码已保存");
            // 返回二维码
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        }


        log.info("调用统一下单API");

        //调用统一下单API
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));

        // 请求body参数
        Gson gson = new Gson();
        Map<String, Object> paramsMap = new HashMap<>(14);
        paramsMap.put("appid", wxPayConfig.getAppid());
        paramsMap.put("mchid", wxPayConfig.getMchId());
        paramsMap.put("description", orderInfo.getTitle());
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));

        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total", orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");
        // 设置金额
        paramsMap.put("amount", amountMap);

        //将参数转换成json字符串
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===> {}" + jsonParams);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        CloseableHttpResponse response = null;
        try {
            response = wxPayClient.execute(httpPost);
            // 获取响应参数
            Map resultMap = buildBodyParams(response, Map.class);
            //二维码
            codeUrl = (String) resultMap.get("code_url");
            //保存二维码
            return saveCodeUrl(orderInfo, codeUrl);
        } finally {
            response.close();
        }
    }


    /**
     * 通知回调-> 更新订单状态逻辑
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException, InterruptedException {
        log.info("处理订单");

        //解密报文
        String plainText = decryptFromResource(bodyMap);

        // 将明文转换成map
        Gson gson = new Gson();
        Map<String, Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String) plainTextMap.get("out_trade_no");


        // 微信特别提醒：
        // 在对业务数据进行状态检查和处理之前，
        // 要采用数据锁进行并发控制，以避免函数重入造成的数据混乱.

        // 尝试获取锁：
        // 成功获取则立即返回true，获取失败则立即返回false。不必一直等待锁的释放.
        if (lock.tryLock()) {
            try {
                // 处理重复的通知
                // 接口调用的幂等性：无论接口被调用多少次，产生的结果是一致的。
                OrderInfo orderInfo = orderInfoService.lambdaQuery().eq(OrderInfo::getOrderNo, (orderNo)).one();

                if (null != orderInfo && !OrderStatus.NOTPAY.getType().equals(orderInfo.getOrderStatus())) {
                    log.info("重复的通知,已经支付成功啦");
                    return;
                }

                // 模拟通知并发
                //TimeUnit.SECONDS.sleep(5);

                // 更新订单状态
                orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).set(OrderInfo::getOrderStatus, OrderStatus.SUCCESS.getType()).update();
                log.info("更新订单状态,订单号：{},订单状态：{}", orderNo, OrderStatus.SUCCESS);
                // 记录支付日志
                paymentInfoService.createPaymentInfo(plainText);
            } finally {
                // 要主动释放锁
                lock.unlock();
            }
        }
    }

    /**
     * 取消订单
     * 业务场景： 用户下单后还未进行支付，进行取消订单
     */
    @Override
    public void cancelOrder(String orderNo) throws Exception {
        // step 1 调用微信关单接口
        closeOrder(orderNo);
    }

    /**
     * 关闭订单
     */
    private void closeOrder(String orderNo) throws IOException {
        // step 1 微信特别提醒 在关闭订单之前查询一下订单在微信服务当中的状态
        WeChartOrderInfo state = selectOrderInfo(orderNo);
        // 还未支付才能进行取消订单
        if (state.getTrade_state().equals(OrderStatus.NOTPAY.name())) {
            // step 2 进行调用微信关闭订单接口
            sendCloseRequest(orderNo);
            // step 2 更新商户端的订单状态
            this.orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).set(OrderInfo::getOrderStatus, OrderStatus.CANCEL.getType()).update();
        }
    }

    /**
     * 获取微信订单信息
     */
    private WeChartOrderInfo selectOrderInfo(String orderNo) throws IOException {
        log.info("查询订单，直连商户号：{} ， 微信支付订单号: {}", orderNo, wxPayConfig.getMchId());
        String url = wxPayConfig.getDomain().concat(String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo)).concat("?mchid=").concat(wxPayConfig.getMchId());
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");
        CloseableHttpResponse response = wxPayClient.execute(httpGet);
        WeChartOrderInfo weChartOrderInfo = buildBodyParams(response, WeChartOrderInfo.class);
        log.info("查询订单响应, {}", weChartOrderInfo);
        response.close();
        return weChartOrderInfo;
    }

    /**
     * 发送关闭订单请求
     *
     * @param orderNo 订单编号
     */
    private void sendCloseRequest(String orderNo) throws IOException {
        log.info("关闭订单， 订单号：{}", orderNo);
        String url = String.format(wxPayConfig.getDomain().concat(WxApiType.CLOSE_ORDER_BY_NO.getType()), orderNo);
        HttpPost httpPost = new HttpPost(url);
        Map<String, Object> paramsMap = new HashMap<>(1);
        paramsMap.put("mchid", wxPayConfig.getMchId());
        Gson gson = new Gson();
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===> {}", jsonParams);
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        CloseableHttpResponse response = wxPayClient.execute(httpPost);
        try {

            log.info("关单响应： {}", response.getEntity());
            log.info("解析body {}", buildBodyParams(response, Map.class));
        } finally {
            response.close();
        }
    }

    /**
     * 查询订单状态
     *
     * @param orderNo 订单编号
     */
    @Override
    public String queryOrder(String orderNo) throws Exception {
        return this.selectOrderInfo(orderNo).getTrade_state();
    }

    /**
     * 检查订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkOrderStatus(String orderNo) throws Exception {

        log.warn("根据订单号核实订单状态 ===> {}", orderNo);

        //调用微信支付查单接口
        WeChartOrderInfo chartOrderInfo = this.selectOrderInfo(orderNo);
        //获取微信支付端的订单状态
        String tradeState = chartOrderInfo.getTrade_state();

        //判断订单状态
        if (WxTradeState.SUCCESS.getType().equals(tradeState)) {

            log.warn("核实订单已支付 ===> {}", orderNo);

            //如果确认订单已支付则更新本地订单状态
            orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).set(OrderInfo::getOrderStatus, OrderStatus.SUCCESS.getType()).update();
            //记录支付日志
            paymentInfoService.createPaymentInfo(new Gson().toJson(chartOrderInfo));
        }

        if (WxTradeState.NOTPAY.getType().equals(tradeState)) {
            log.warn("核实订单未支付 ===> {}", orderNo);

            //如果订单未支付，则调用关单接口
            this.closeOrder(orderNo);

            //更新本地订单状态
            orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).set(OrderInfo::getOrderStatus, OrderStatus.CLOSED.getType()).update();
        }

    }

    /**
     * 申请退款
     */
    @Override
    public void refund(String orderNo, String reason, String refundsNo) throws Exception {
        log.info("校验开始");
        PaymentInfo paymentInfo = paymentInfoService.lambdaQuery().eq(PaymentInfo::getOrderNo, orderNo).one();
        if (null == paymentInfo) {
            throw new RuntimeException("未查询到该订单,请稍后再试!");
        }
        String transactionNo = paymentInfo.getTransactionId().substring(paymentInfo.getTransactionId().length() - 4);
        if (!transactionNo.equals(refundsNo)) {
            throw new RuntimeException("这笔可能不是你的订单哦.请核实支付成功后微信通知当中的交易订单号后四位!");
        }

        log.info("创建退款单记录");
        //根据订单编号创建退款单
        RefundInfo refundsInfo = refundsInfoService.createRefundByOrderNo(orderNo, reason);

        log.info("调用退款API");

        //调用统一下单API
        String url = wxPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType());
        HttpPost httpPost = new HttpPost(url);

        // 请求body参数
        Gson gson = new Gson();
        Map paramsMap = new HashMap();
        paramsMap.put("out_trade_no", orderNo);//订单编号
        paramsMap.put("out_refund_no", refundsInfo.getRefundNo());//退款单编号
        paramsMap.put("reason", reason);//退款原因
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));//退款通知地址

        Map amountMap = new HashMap();
        amountMap.put("refund", refundsInfo.getRefund());//退款金额
        amountMap.put("total", refundsInfo.getTotalFee());//原订单金额
        amountMap.put("currency", "CNY");//退款币种
        paramsMap.put("amount", amountMap);

        //将参数转换成json字符串
        String jsonParams = gson.toJson(paramsMap);
        log.info("请求参数 ===> {}" + jsonParams);

        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");//设置请求报文格式
        httpPost.setEntity(entity);//将请求报文放入请求对象
        httpPost.setHeader("Accept", "application/json");//设置响应报文格式

        //完成签名并执行请求，并完成验签
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {

            //解析响应结果
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功, 退款返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("退款异常, 响应码 = " + statusCode + ", 退款返回结果 = " + bodyAsString);
            }

            //更新订单状态
            orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).set(OrderInfo::getOrderStatus, OrderStatus.REFUND_PROCESSING.getType()).update();
            //更新退款单
            refundsInfoService.updateRefund(bodyAsString);

        } finally {
            response.close();
        }

    }

    private void old(String orderNo, String reason) throws IOException {
        // 查询订单信息
        OrderInfo orderInfo = this.orderInfoService.lambdaQuery().eq(OrderInfo::getOrderNo, orderNo).one();

        log.info("申请退款订单号：{} ， 退款原因: {}", orderNo, reason);
        String url = wxPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType());
        HttpPost httpPost = new HttpPost(url);
        Map<String, Object> paramsMap = new HashMap<>(1);
        paramsMap.put("out_trade_no", orderNo);
        paramsMap.put("out_refund_no", orderNo + "_" + "refund");
        paramsMap.put("reason", reason);
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));
        // 金额信息
        HashMap<String, Object> amount = new HashMap<>();
        amount.put("refund", orderInfo.getTotalFee()); // 退款金额
        amount.put("total", orderInfo.getTotalFee()); // 原退款金额
        amount.put("currency", "CNY"); // 退款金额
        paramsMap.put("amount", amount);
        String jsonParams = new Gson().toJson(paramsMap);
        log.info("请求参数 ===> {}", jsonParams);
        StringEntity entity = new StringEntity(jsonParams, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        CloseableHttpResponse response = wxPayClient.execute(httpPost);
        Map bodyParams = buildBodyParams(response, Map.class);
        log.info("查询订单响应, {}", bodyParams);
        response.close();
    }

    /**
     * 查询退款订单
     *
     * @param refundNo 退款编号
     * @return
     * @throws Exception
     */
    @Override
    public String queryRefund(String refundNo) throws Exception {

        log.info("查询退款接口调用 ===> {}", refundNo);

        String url = String.format(WxApiType.DOMESTIC_REFUNDS_QUERY.getType(), refundNo);
        url = wxPayConfig.getDomain().concat(url);

        //创建远程Get 请求对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpGet);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功, 查询退款返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("查询退款异常, 响应码 = " + statusCode + ", 查询退款返回结果 = " + bodyAsString);
            }

            return bodyAsString;

        } finally {
            response.close();
        }
    }

    /**
     * 检查退款订单状态
     * 场景：用户退款成功 由于微信发送退款通知给到商户系统 商户系统因部分原因不能处理，
     * 那么商户应该主动调用微信查询退款单接口进行更新订单状态
     *
     * @param refundNo
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkRefundStatus(String refundNo) throws Exception {

        log.warn("根据退款单号核实退款单状态 ===> {}", refundNo);

        //调用查询退款单接口
        String result = this.queryRefund(refundNo);

        //组装json请求体字符串
        Gson gson = new Gson();
        Map<String, String> resultMap = gson.fromJson(result, HashMap.class);

        // 获取微信支付端退款状态
        String status = resultMap.get("status");
        String orderNo = resultMap.get("out_trade_no");

        if (WxRefundStatus.SUCCESS.getType().equals(status)) {

            log.warn("核实订单已退款成功 ===> {}", refundNo);

            //如果确认退款成功，则更新订单状态
            orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).set(OrderInfo::getOrderStatus, OrderStatus.REFUND_SUCCESS.getType()).update();

            //更新退款单
            refundsInfoService.updateRefund(result);
        }

        if (WxRefundStatus.ABNORMAL.getType().equals(status)) {

            log.warn("核实订单退款异常  ===> {}", refundNo);

            // 退款异常
            orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).set(OrderInfo::getOrderStatus, OrderStatus.REFUND_ABNORMAL.getType()).update();

            // 更新退款单
            refundsInfoService.updateRefund(result);
        }
    }

    /**
     * 退款通知 处理退款单 数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processRefund(Map<String, Object> bodyMap) throws Exception {

        log.info("退款单");

        //解密报文
        String plainText = decryptFromResource(bodyMap);

        //将明文转换成map
        Gson gson = new Gson();
        HashMap plainTextMap = gson.fromJson(plainText, HashMap.class);
        String orderNo = (String) plainTextMap.get("out_trade_no");

        if (lock.tryLock()) {
            try {

                String orderStatus = orderInfoService.lambdaQuery().eq(OrderInfo::getOrderNo, orderNo).one().getOrderStatus();
                if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderStatus)) {
                    return;
                }

                //更新订单状态
                orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).set(OrderInfo::getOrderStatus, OrderStatus.REFUND_SUCCESS.getType()).update();
                //更新退款单
                refundsInfoService.updateRefund(plainText);
            } finally {
                //要主动释放锁
                lock.unlock();
            }
        }
    }

    /**
     * 申请账单
     */
    @Override
    public tradeBillResponse queryBill(String billDate, String type) throws IOException {
        log.warn("申请账单接口调用 {}", billDate);

        String url;
        if ("tradebill".equals(type)) {
            url = WxApiType.TRADE_BILLS.getType();
        } else if ("fundflowbill".equals(type)) {
            url = WxApiType.FUND_FLOW_BILLS.getType();
        } else {
            throw new RuntimeException("不支持的账单类型");
        }
        url = wxPayConfig.getDomain().concat(url)
                .concat("?bill_date=").concat(billDate)
                .concat("&sub_mchid=").concat(wxPayConfig.getMchId())
//                .concat("&bill_type=ALL")
        ;
        // 创建远程Get 请求对象
        log.info("url:{}", url);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");
        // https://api.mch.weixin.qq.com/v3/bill/tradebill?bill_date=2023-05-18&sub_mchid=1634543682&bill_type=ALL
        // https://api.mch.weixin.qq.com/v3/bill/tradebill?bill_date=2019-06-11&sub_mchid=1900000001&bill_type=ALL
        // 完成签名并执行请求
        try (CloseableHttpResponse response = wxPayClient.execute(httpGet)) {
            return buildBodyParams(response, tradeBillResponse.class);
        }
    }

    /**
     * 下载账单 可查看的url
     *
     * @param billDate 账单日期
     * @param type     下载账单类型 tradebill 申请交易账单  fundflowbill 申请资金账单
     */
    @Override
    public String downloadBill(String billDate, String type) throws Exception {
        log.warn("下载账单接口调用 {}, {}", billDate, type);

        //获取账单url地址
        tradeBillResponse tradeBillResponse = this.queryBill(billDate, type);
        //创建远程Get 请求对象
        HttpGet httpGet = new HttpGet(tradeBillResponse.getDownload_url());
        httpGet.addHeader("Accept", "application/json");

        //使用wxPayClient发送请求得到响应
        CloseableHttpResponse response = null;
        try {
            response = wxPayNoSignClient.execute(httpGet);
            return EntityUtils.toString(response.getEntity());
        } finally {
            response.close();
        }
    }

    /**
     * V2 的 支付
     *
     * @param productId
     * @param remoteAddr
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> nativePayV2(Long productId, String remoteAddr) throws Exception {

        log.info("生成订单");

        //生成订单
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
        String codeUrl = orderInfo.getCodeUrl();
        if (orderInfo != null && !StringUtils.isEmpty(codeUrl)) {
            log.info("订单已存在，二维码已保存");
            //返回二维码
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        }

        log.info("调用统一下单API");

        //  调用支付接口
        HttpClientUtils client = new HttpClientUtils(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY_V2.getType()));

        //组装接口参数
        Map<String, String> params = new HashMap<>();
        params.put("appid", wxPayConfig.getAppid());//关联的公众号的appid
        params.put("mch_id", wxPayConfig.getMchId());//商户号
        params.put("nonce_str", WXPayUtil.generateNonceStr());//生成随机字符串
        params.put("body", orderInfo.getTitle());
        params.put("out_trade_no", orderInfo.getOrderNo());

        //注意，这里必须使用字符串类型的参数（总金额：分）
        String totalFee = orderInfo.getTotalFee() + "";
        params.put("total_fee", totalFee);

        params.put("spbill_create_ip", remoteAddr);
        params.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY_V2.getType()));
        params.put("trade_type", "NATIVE");

        //将参数转换成xml字符串格式：生成带有签名的xml格式字符串
        String xmlParams = WXPayUtil.generateSignedXml(params, wxPayConfig.getPartnerKey());
        log.info("\n xmlParams：\n" + xmlParams);

        client.setXmlParam(xmlParams);//将参数放入请求对象的方法体
        client.setHttps(true);//使用https形式发送
        client.post();//发送请求
        String resultXml = client.getContent();//得到响应结果
        log.info("\n resultXml：\n" + resultXml);
        //将xml响应结果转成map对象
        Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);

        //错误处理
        if ("FAIL".equals(resultMap.get("return_code")) || "FAIL".equals(resultMap.get("result_code"))) {
            log.error("微信支付统一下单错误 ===> {} ", resultXml);
            throw new RuntimeException("微信支付统一下单错误");
        }

        //二维码
        codeUrl = resultMap.get("code_url");
        return saveCodeUrl(orderInfo, codeUrl);
    }

    /**
     * 保存二维码
     *
     * @param orderInfo 订单信息
     * @param codeUrl   保存二维码
     */
    private Map<String, Object> saveCodeUrl(OrderInfo orderInfo, String codeUrl) {
        //保存二维码
        orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderInfo.getOrderNo()).set(OrderInfo::getCodeUrl, codeUrl).update();
        //返回二维码
        Map<String, Object> map = new HashMap<>();
        map.put("codeUrl", codeUrl);
        map.put("orderNo", orderInfo.getOrderNo());
        return map;
    }

    /**
     * 对称解密
     */
    private String decryptFromResource(Map<String, Object> bodyMap) throws GeneralSecurityException {
        log.info("密文解密");
        //通知数据拿到 resource 节点
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        //数据密文
        String ciphertext = resourceMap.get("ciphertext");
        //随机串
        String nonce = resourceMap.get("nonce");
        //附加数据
        String associatedData = resourceMap.get("associated_data");
        log.info("密文 ===> {}", ciphertext);
        AesUtil aesUtil = new AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        // 使用key、nonce和associated_data，对数据密文resource.ciphertext进行解密，得到JSON形式的资源对象
        String plainText = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8), ciphertext);
        log.info("明文 ===> {}", plainText);

        return plainText;
    }


    /**
     * 解析响应参数
     */
    private <T> T buildBodyParams(CloseableHttpResponse response, Class<T> tClass) throws IOException {
        T bodyAsString = null;

        if (null != response.getEntity()) {
            Gson gson = new Gson();
            String json = EntityUtils.toString(response.getEntity());//响应体
            bodyAsString = gson.fromJson(json, tClass);
        }

        int statusCode = response.getStatusLine().getStatusCode();//响应状态码
        if (statusCode == 200) { //处理成功
            log.info("成功, 返回结果 = " + bodyAsString);
        } else if (statusCode == 204) { //处理成功，无返回Body
            log.info("成功");
        } else if (statusCode == 404) { //处理成功，无返回Body
            log.info("没找到订单...");
        } else {
            log.info("响应：{}, {}",response.getEntity(),response.getStatusLine());
            log.info("失败,响应码 = " + statusCode + ",返回结果 = " + bodyAsString);
            throw new IOException("request failed");
        }
        return bodyAsString;
    }

}
