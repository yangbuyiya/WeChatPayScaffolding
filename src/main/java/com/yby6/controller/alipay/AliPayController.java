// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.controller.alipay;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import com.yby6.entity.AliPayBean;
import com.yby6.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * ../
 *
 * @author Yang Shuai
 * Create By 2023/05/21
 */
@Controller
@RequestMapping("/aliPay")
public class AliPayController extends AbstractAliPayApiController {
    private static final Logger log = LoggerFactory.getLogger(AliPayController.class);

    @Resource
    private AliPayBean aliPayBean;
    // 普通公钥模式
    private final static String NOTIFY_URL = "/aliPay/notify_url";
    private final static String RETURN_URL = "/aliPay/return_url";


    /**
     * 获取支付宝配置
     *
     * @return {@link AliPayApiConfig} 支付宝配置
     * @throws AlipayApiException 支付宝 Api 异常
     */
    @Override
    public AliPayApiConfig getApiConfig() throws AlipayApiException {
        AliPayApiConfig aliPayApiConfig;
        try {
            aliPayApiConfig = AliPayApiConfigKit.getApiConfig(aliPayBean.getAppId());
        } catch (Exception e) {
            aliPayApiConfig = AliPayApiConfig.builder()
                    .setAppId(aliPayBean.getAppId())
                    .setAliPayPublicKey(aliPayBean.getPublicKey())
                    .setAppCertPath(aliPayBean.getAppCertPath())
                    .setAliPayCertPath(aliPayBean.getAliPayCertPath())
                    .setAliPayRootCertPath(aliPayBean.getAliPayRootCertPath())
                    .setCharset("UTF-8")
                    .setPrivateKey(aliPayBean.getPrivateKey())
                    .setServiceUrl(aliPayBean.getServerUrl())
                    .setSignType("RSA2")
                    .build();
        }
        return aliPayApiConfig;
    }


    @RequestMapping("/test")
    @ResponseBody
    public AliPayApiConfig test() {
        AliPayApiConfig aliPayApiConfig = AliPayApiConfigKit.getAliPayApiConfig();
        String charset = aliPayApiConfig.getCharset();
        log.info("charset>" + charset);
        return aliPayApiConfig;
    }


    /**
     * PC支付
     */
    @RequestMapping(value = "/pcPay/{totalAmount}")
    @ResponseBody
    public void pcPay(@PathVariable String totalAmount, HttpServletResponse response) {
        try {
            String outTradeNo = StringUtils.getOutTradeNo();
            log.info("pc outTradeNo > {}", outTradeNo);

            String returnUrl = aliPayBean.getDomain() + RETURN_URL;
            String notifyUrl = aliPayBean.getDomain() + NOTIFY_URL;
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();

            model.setOutTradeNo(outTradeNo);
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            model.setTotalAmount(totalAmount);
            model.setSubject("Javen PC支付测试");
            model.setBody("Javen IJPay PC支付测试");
            model.setPassbackParams("passback_params");
            /*
              花呗分期相关的设置,测试环境不支持花呗分期的测试
              hb_fq_num代表花呗分期数，仅支持传入3、6、12，其他期数暂不支持，传入会报错；
              hb_fq_seller_percent代表卖家承担收费比例，商家承担手续费传入100，用户承担手续费传入0，仅支持传入100、0两种，其他比例暂不支持，传入会报错。
             */
//            ExtendParams extendParams = new ExtendParams();
//            extendParams.setHbFqNum("3");
//            extendParams.setHbFqSellerPercent("0");
//            model.setExtendParams(extendParams);

            AliPayApi.tradePage(response, model, notifyUrl, returnUrl);
            // https://opensupport.alipay.com/support/helpcenter/192/201602488772?ant_source=antsupport
            // Alipay Easy SDK（新版）目前只支持输出form表单，不支持打印出url链接。
//            AliPayApi.tradePage(response, "GET", model, notifyUrl, returnUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 扫码支付
     */
    @RequestMapping(value = "/tradePreCreatePay")
    @ResponseBody
    public void tradePreCreatePay(HttpServletResponse response) {
        String subject = "Javen 支付宝扫码支付测试";
        String totalAmount = "0.01";
        String storeId = "123";
        String notifyUrl = aliPayBean.getDomain() + NOTIFY_URL;
//		String notifyUrl = aliPayBean.getDomain() + "/aliPay/cert_notify_url";

        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setSubject(subject);
        model.setTotalAmount(totalAmount);
        model.setStoreId(storeId);
        model.setTimeoutExpress("5m");
        model.setOutTradeNo(StringUtils.getOutTradeNo());
        try {
            String resultStr = AliPayApi.tradePrecreatePayToResponse(model, notifyUrl).getBody();
            JSONObject jsonObject = JSONObject.parseObject(resultStr);

            // jsonObject.getJSONObject("alipay_trade_precreate_response").getString("qr_code")

            // 生成指定url对应的二维码到文件，宽和高都是300像素
            String qrcode = jsonObject.getJSONObject("alipay_trade_precreate_response").getString("qr_code");
            ServletOutputStream outputStream = response.getOutputStream();
            QrCodeUtil.generate(qrcode, 300, 300, "", outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 支付宝资金账户资产查询接口
     */
    @RequestMapping(value = "/accountQuery")
    @ResponseBody
    public String accountQuery(@RequestParam(required = true, name = "aliPayUserId") String aliPayUserId) {
        AlipayFundAccountQueryModel model = new AlipayFundAccountQueryModel();
        model.setAlipayUserId(aliPayUserId);
        model.setAccountType("ACCTRANS_ACCOUNT");
        try {
            return AliPayApi.accountQueryToResponse(model, null).getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载对账单
     */
    @RequestMapping(value = "/dataDataServiceBill")
    @ResponseBody
    public String dataDataServiceBill(@RequestParam("billDate") String billDate) {
        try {
            AlipayDataDataserviceBillDownloadurlQueryModel model = new AlipayDataDataserviceBillDownloadurlQueryModel();
            model.setBillType("trade");
            model.setBillDate(billDate);
            return AliPayApi.billDownloadUrlQuery(model);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 支付宝退款 不退手续费
     */
    @RequestMapping(value = "/tradeRefund")
    @ResponseBody
    public String tradeRefund(@RequestParam(required = false, name = "outTradeNo") String outTradeNo,
                              @RequestParam(required = false, name = "tradeNo") String tradeNo) {

        try {
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            if (StringUtils.isNotEmpty(outTradeNo)) {
                model.setOutTradeNo(outTradeNo);
            }
            if (StringUtils.isNotEmpty(tradeNo)) {
                model.setTradeNo(tradeNo);
            }
            model.setRefundAmount("1");
            model.setRefundReason("正常退款");
            return AliPayApi.tradeRefundToResponse(model).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 交易查询
     */
    @RequestMapping(value = "/tradeQuery")
    @ResponseBody
    public String tradeQuery(@RequestParam(required = false, name = "outTradeNo") String outTradeNo, @RequestParam(required = false, name = "tradeNo") String tradeNo) {
        try {
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            if (StringUtils.isNotEmpty(outTradeNo)) {
                model.setOutTradeNo(outTradeNo);
            }
            if (StringUtils.isNotEmpty(tradeNo)) {
                model.setTradeNo(tradeNo);
            }
            return AliPayApi.tradeQueryToResponse(model).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 创建订单
     * {"alipay_trade_create_response":{"code":"10000","msg":"Success","out_trade_no":"081014283315033","trade_no":"2017081021001004200200274066"},"sign":"ZagfFZntf0loojZzdrBNnHhenhyRrsXwHLBNt1Z/dBbx7cF1o7SZQrzNjRHHmVypHKuCmYifikZIqbNNrFJauSuhT4MQkBJE+YGPDtHqDf4Ajdsv3JEyAM3TR/Xm5gUOpzCY7w+RZzkHevsTd4cjKeGM54GBh0hQH/gSyhs4pEN3lRWopqcKkrkOGZPcmunkbrUAF7+AhKGUpK+AqDw4xmKFuVChDKaRdnhM6/yVsezJFXzlQeVgFjbfiWqULxBXq1gqicntyUxvRygKA+5zDTqE5Jj3XRDjVFIDBeOBAnM+u03fUP489wV5V5apyI449RWeybLg08Wo+jUmeOuXOA=="}
     */
    @RequestMapping(value = "/tradeCreate")
    @ResponseBody
    public String tradeCreate(@RequestParam("outTradeNo") String outTradeNo) {

        String notifyUrl = aliPayBean.getDomain() + NOTIFY_URL;

        AlipayTradeCreateModel model = new AlipayTradeCreateModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount("1");
        model.setBody("Body");
        model.setSubject("Javen 测试统一收单交易创建接口");
        //买家支付宝账号，和buyer_id不能同时为空
        model.setBuyerLogonId("17773596535");
        try {
            AlipayTradeCreateResponse response = AliPayApi.tradeCreateToResponse(model, notifyUrl);
            return response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 撤销订单
     */
    @RequestMapping(value = "/tradeCancel")
    @ResponseBody
    public String tradeCancel(@RequestParam(required = false, name = "outTradeNo") String outTradeNo, @RequestParam(required = false, name = "tradeNo") String tradeNo) {
        try {
            AlipayTradeCancelModel model = new AlipayTradeCancelModel();
            if (StringUtils.isNotEmpty(outTradeNo)) {
                model.setOutTradeNo(outTradeNo);
            }
            if (StringUtils.isNotEmpty(tradeNo)) {
                model.setTradeNo(tradeNo);
            }

            return AliPayApi.tradeCancelToResponse(model).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭订单
     */
    @RequestMapping(value = "/tradeClose")
    @ResponseBody
    public String tradeClose(@RequestParam("outTradeNo") String outTradeNo, @RequestParam("tradeNo") String tradeNo) {
        try {
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            if (StringUtils.isNotEmpty(outTradeNo)) {
                model.setOutTradeNo(outTradeNo);
            }
            if (StringUtils.isNotEmpty(tradeNo)) {
                model.setTradeNo(tradeNo);
            }

            return AliPayApi.tradeCloseToResponse(model).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用授权URL并授权
     */
    @RequestMapping(value = "/toOauth")
    @ResponseBody
    public void toOauth(HttpServletResponse response) {
        try {
            String redirectUri = aliPayBean.getDomain() + "/aliPay/redirect_uri";
            String oauth2Url = AliPayApi.getOauth2Url(aliPayBean.getAppId(), redirectUri);
            response.sendRedirect(oauth2Url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 应用授权回调
     */
    @RequestMapping(value = "/redirect_uri")
    @ResponseBody
    public String redirectUri(@RequestParam("app_id") String appId, @RequestParam("app_auth_code") String appAuthCode) {
        try {
            System.out.println("app_id:" + appId);
            System.out.println("app_auth_code:" + appAuthCode);
            //使用app_auth_code换取app_auth_token
            AlipayOpenAuthTokenAppModel model = new AlipayOpenAuthTokenAppModel();
            model.setGrantType("authorization_code");
            model.setCode(appAuthCode);
            return AliPayApi.openAuthTokenAppToResponse(model).getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询授权信息
     */
    @RequestMapping(value = "/openAuthTokenAppQuery")
    @ResponseBody
    public String openAuthTokenAppQuery(@RequestParam("appAuthToken") String appAuthToken) {
        try {
            AlipayOpenAuthTokenAppQueryModel model = new AlipayOpenAuthTokenAppQueryModel();
            model.setAppAuthToken(appAuthToken);
            return AliPayApi.openAuthTokenAppQueryToResponse(model).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }


    @RequestMapping(value = "/return_url")
    @ResponseBody
    public String returnUrl(HttpServletRequest request) {
        try {
            // 获取支付宝GET过来反馈信息
            Map<String, String> map = AliPayApi.toMap(request);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

            boolean verifyResult = AlipaySignature.rsaCheckV1(map, aliPayBean.getPublicKey(), "UTF-8",
                    "RSA2");

            if (verifyResult) {
                // TODO 请在这里加上商户的业务逻辑程序代码
                System.out.println("return_url 验证成功");
                return "success";
            } else {
                System.out.println("return_url 验证失败");
                // TODO
                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return "failure";
        }
    }


    /***
     * 异步通知
     * @param request
     * @return
     */
    @RequestMapping(value = "/notify_url")
    @ResponseBody
    public String notifyUrl(HttpServletRequest request) {
        try {
            // 获取支付宝POST过来反馈信息
            Map<String, String> params = AliPayApi.toMap(request);

            for (Map.Entry<String, String> entry : params.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

            boolean verifyResult = AlipaySignature.rsaCheckV1(params, aliPayBean.getPublicKey(), "UTF-8", "RSA2");

            if (verifyResult) {
                // TODO 请在这里加上商户的业务逻辑程序代码 异步通知可能出现订单重复通知 需要做去重处理
                System.out.println("notify_url 验证成功succcess");
                return "success";
            } else {
                System.out.println("notify_url 验证失败");
                // TODO
                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return "failure";
        }
    }

}
