<template>
  <view class="app-container">
    <u-notice-bar color="red" text="注意：小程序可查看有限只渲染近50条支付信息,完整请前往PC端!!!"></u-notice-bar>

    <view class="PaymentChannel_title">
      <u-tooltip text="PC端微信支付系统: https://lzys522.cn/wx" copyText="https://lzys522.cn/wx"
                 overlay></u-tooltip>
    </view>
    <view class="PaymentChannel_title">
      注意：0.4以上,支付成功后可在订单列表进行退款.
    </view>

    <u-loading-page :loading="loading">
    </u-loading-page>

    <view>
      <u-subsection
          :list="queryList"
          mode="button"
          :current="currentSubsection"
          @change="change1"
      ></u-subsection>
    </view>


    <view class="u-page">
      <u-list v-if="list.length>0"
              @scrolltolower="scrolltolower"
      >
        <u-list-item
            v-for="(item, index) in list"
            :key="index"
        >
          <u-cell>
            <view slot="title">
              <view>
                <view>
                  <view style="display: inline-block">
                    <u-tag :text="reloadTitle(item.title.split('-')[0])" plain plainFill></u-tag>
                    <!--                                        《{{item.title.split('-')[0]}}》-->
                  </view>

                  <view class="payView">
                    <view style="width: 180rpx;">
                      <u-tag :text="item.orderStatus"
                             v-if="item.orderStatus === '未支付'"></u-tag>
                      <u-tag :text="item.orderStatus" v-if="item.orderStatus === '支付成功'"
                             type="success"></u-tag>
                      <u-tag :text="item.orderStatus" v-if="item.orderStatus === '超时已关闭'"
                             type="warning"></u-tag>
                      <u-tag :text="item.orderStatus" v-if="item.orderStatus === '用户已取消'"
                             type="info"></u-tag>
                      <u-tag :text="item.orderStatus" v-if="item.orderStatus === '退款中'"
                             type="danger"></u-tag>
                      <u-tag :text="item.orderStatus" v-if="item.orderStatus === '已退款'"
                             type="info"></u-tag>
                    </view>
                  </view>

                </view>
                <view>
                  订单支付人: {{ item.title.split('-')[1] ? item.title.split('-')[1] : '未知' }}
                </view>
              </view>
            </view>


            <view slot="label">
              <view style="float:left;">
                <view style="text-align: left;">
                  订单金额：{{ item.totalFee / 100 }} 元
                </view>
                <view style="text-align: left;">
                  下单时间: {{ item.createTime }}
                </view>
              </view>
              <view style="float:right;">

                <view class="btn" v-if="item.orderStatus === '支付成功'">
                  <u-button size="small" type="primary"
                            @click="refund(item)"
                            text="退款"></u-button>
                </view>

                <view class="btn" v-if="item.orderStatus === '未支付'">
                  <u-button size="small" type="primary" :disabled="payBtnDisabled"
                            @click="toPay(item)"
                            text="支付"></u-button>
                </view>

                <view class="btn" v-if="item.orderStatus === '未支付'">
                  <u-button size="small" type="error"
                            @click="cancelOrder(item)"
                            text="取消"></u-button>
                </view>
              </view>
            </view>
          </u-cell>

        </u-list-item>
      </u-list>


      <view v-else style="margin-top: 15px;">
        <u-empty icon="https://cdn.uviewui.com/uview/empty/data.png"></u-empty>
      </view>


    </view>
    <u-modal
        :loading="loading"
        title="申请退款"
        :show="refundDialogVisible"
        showCancelButton
        @confirm="refundOrder"
        @cancel="closeDialog "
    >
      <view>
        <!-- 注意，如果需要兼容微信小程序，最好通过setRules方法设置rules规则 -->
        <u-form
            labelPosition="left"
            :model="refundForm"
            :rules="rules"
            ref="uForm"
        >
          <u-form-item
              labelWidth="100px"
              label="交易订单号"
              prop="refundNo"
              borderBottom
              ref="item1"
          >
            <u-input
                v-model="refundForm.refundNo"
                border="none"
            ></u-input>
          </u-form-item>
          <u-form-item
              labelWidth="100px"
              label="退款原因"
              prop="reason"
              borderBottom
              ref="item2"
          >
            <u-input
                v-model="refundForm.reason"
                border="none"
            ></u-input>
          </u-form-item>
        </u-form>
      </view>
    </u-modal>
  </view>
</template>

<script>
import orderInfoApi from "@/api/orderInfo"
import {cancel, jsapi, refunds} from "@/api/wechatPay";
import {toast} from "@/utils/common";

export default {
  data() {
    return {
      loading: false,
      loadingPageData: {
        // 自定义提示内容
        loadingText: '',
        // 自定义图片
        image: '',
        // 自定义加载动画模式
        loadingMode: '',
        // 自定义背景色
        bgColor: '#ffffff',
      },
      list: [],
      refundDialogVisible: false,
      refundForm: {
        orderNo: '',
        refundNo: '',
        reason: '',
      },
      refundSubmitBtnDisabled: false,
      rules: {
        'refundNo': {
          type: 'number',
          required: true,
          max: 4,
          message: '请填写后四位交易订单号(数字)',
          trigger: ['blur', 'change']
        },
        'reason': {
          type: 'string',
          required: true,
          message: '请填写退款原因',
          trigger: ['blur', 'change']
        },
      },
      payBtnDisabled: false,
      queryList: ['全部', '未支付', '退款中', '已退款', '支付成功', '超时关闭', '用户取消'],
      currentSubsection: 0,
    }
  },
  onShow() {
    this.loadmore()
  },
  onReady() {
    //onReady 为uni-app支持的生命周期之一
    this.$refs.uForm.setRules(this.rules)
  },
  methods: {
    reloadTitle(item) {
      return "《" + item + "》"
    },
    // 类型切换
    change1(index) {
      this.currentSubsection = index
      // 发送请求
      this.loadmore()
    },
    //退款对话框
    refund(item) {
      // orderNo, total, refundNo
      if ((item.totalFee / 100) < 0.5) {
        toast("金额太少就不退了,感谢老板的支持!")
        return;
      }
      this.refundDialogVisible = true
      this.refundForm.orderNo = item.orderNo
      this.refundForm.refundNo = item.refundNo
    },
    refundOrder() {
      if (this.refundSubmitBtnDisabled) {
        return;
      }
      this.loading = true
      // if ([undefined, null, ''].includes(this.refundNo) || this.refundNo.length < 4) {
      //     toast("请输入四位交易订单号")
      //     return
      // }
      console.log(this.refundForm);
      this.$refs.uForm.validate().then(res => {
        this.refundSubmitBtnDisabled = true //禁用按钮，防止重复提交
        refunds(this.refundForm.orderNo, this.refundForm.reason, this.refundForm.refundNo).then(response => {
          console.log('response', response)
          toast("申请退款成功,请等待...")
          this.closeDialog()
          this.loadmore()
        }).catch(err => {
          console.log(err);
          this.refundSubmitBtnDisabled = false
        })
        this.refundDialogVisible = true
        this.loading = false

      }).catch(errors => {
        this.refundSubmitBtnDisabled = false
        this.loading = false
      })
    },
    //关闭退款对话框
    closeDialog() {
      console.log('close.................')
      this.refundDialogVisible = false
      this.refundForm = {
        orderNo: '',
        refundNo: '',
        reason: '',
      }
      this.refundSubmitBtnDisabled = false
    },

    // 取消订单
    cancelOrder(item) {
      this.loading = true
      cancel(item.orderNo).then(res => {
        toast("订单取消成功")
        this.loadmore()
      })
    },
    // 未支付的继续支付
    toPay(item) {
      console.log(item);
      this.payBtnDisabled = true
      // 获取微信支付凭证创建支付订单
      const storageSync = uni.getStorageSync('token'); // openId
      const nickName = uni.getStorageSync('nickName');
      jsapi(
          item.productId, storageSync + "|" + nickName
      ).then(res => {
        console.log(res)

        toast("创建订单成功正在拉起支付请稍等....")

        const wx = res.data.wx
        // 调用微信支付弹窗
        uni.requestPayment({
          provide: 'wxpay',
          timeStamp: wx.timeStamp, // 当前时间
          nonceStr: wx.nonceStr, // 随机字符串
          package: wx.package, // prepayId
          signType: wx.signType, // 签名算法
          paySign: wx.paySign, // 支付签名
          success: (res) => {
            console.log(res);

            this.payBtnDisabled = false
            // 支付完毕 短轮询查询后台订单是否成功
            // this.$post(this.api.searchPaymentResult + outTradeNo, {}, (resp) => {
            //     console.log(resp.data);
            //     if (resp.data.result) {
            //         this.$toast.msgSuccess("支付成功")
            //     } else {
            //         this.$toast.msgError("付款异常，请联系客服!")
            //     }
            //
            //     // 等待跳转
            //     setTimeout(() => {
            //         this.$toast.switchTab('/pages/registration_list/registration_list')
            //     }, 2000)
            //
            // })
          },
          fail: (res) => {
            console.log(res);
            toast("取消支付可继续点击支付重新发起")
            this.payBtnDisabled = false
          }
        })
      })
    },


    // 触发到底部的时候调用
    scrolltolower(data) {
      console.log(data);
    },
    loadmore() {
      const data = this.queryList[this.currentSubsection] || null
      this.loading = true
      orderInfoApi.list(data).then((response) => {
        this.list = response.data.list.slice(0, 50);
        this.loading = false
      });
      console.log(this.list);
    }
  },
}
</script>

<style lang="scss">
/deep/ .u-cell__body__content {
  width: 70%;
}

.btn {
  margin-left: 6px;
  display: inline-block;
}

.payView {
  /*float:right*/
  display: inline-block;
  text-align: right;
  vertical-align: baseline;
  float: right;
}

</style>
