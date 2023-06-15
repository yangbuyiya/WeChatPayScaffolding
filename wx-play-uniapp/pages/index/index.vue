<!-- yangbuyi Copyright (c) https://yby6.com 2023. -->

<template>
    <view class="app-container">
        <view class="bg-fa of">
            <section id="index" class="container">

                <u-notice-bar color="red"
                              text="本案例使用JSAPI模式拉取微信支付弹出用户进行微信支付操作。"></u-notice-bar>

                <view class="PaymentChannel_title">
                    <u-tooltip text="个人博客网站: https://yby6.com" copyText="https://yby6.com" overlay></u-tooltip>
                </view>
                <view class="PaymentChannel_title">
                    <u-tooltip text="PC端微信支付系统: https://lzys522.cn/wx" copyText="https://lzys522.cn/wx"
                               overlay></u-tooltip>
                </view>
                <view class="PaymentChannel_title">
                    <u-tooltip text="博客项目案例: https://lzys522.cn" copyText="https://lzys522.cn"
                               overlay></u-tooltip>
                </view>


                <ul v-if="productList.length > 0">
                    <li v-for="product in productList" :key="product.id">
                        <a :class="['orderBtn', {current:payOrder.productId === product.id}]"
                           @click="selectItem(product.id)"
                           href="javascript:void(0);">
                            {{ product.title }}
                            ¥{{ product.price / 100 }}
                        </a>
                    </li>
                </ul>


                <view class="PaymentChannel_payment-channel-panel">
                    <view class="PaymentChannel_title"
                          style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">
                        注意：0.5以上,支付成功后可在订单列表进行退款,此案例只是Demo走完全流程
                    </view>

                    <view class="PaymentChannel_title"
                          style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">
                        需要各大机构课程的私我Wechat：
                        yangbuyiya
                    </view>
                    <h3 class="PaymentChannel_title">
                        <span class="c-333">选择支付方式</span>
                    </h3>
                    <view class="PaymentChannel_channel-options">

                        <!-- 选择微信 data-v-57280228 ChannelOption_payment-channel-option  -->
                        <view :class="[{current:payOrder.payType === 'wxpay'},'ChannelOption_payment-channel-option']"
                              @click="selectPayType('wxpay')">
                            <view class="ChannelOption_channel-icon">
                                <img src="../../static/img/wxpay.png" class="ChannelOption_icon">
                            </view>
                            <view class="ChannelOption_channel-info">
                                <view class="ChannelOption_channel-label">
                                    <view class="ChannelOption_label">微信支付</view>
                                    <view class="ChannelOption_sub-label"></view>
                                    <view class="ChannelOption_check-option"></view>
                                </view>
                            </view>
                        </view>
                        <!-- 选择支付宝 -->
                        <view :class="['ChannelOption_payment-channel-option', {current:payOrder.payType === 'alipay'}]"
                              @click="selectPayType('alipay')">
                            <view class="ChannelOption_channel-icon">
                                <img src="../../static/img/alipay.png" class="ChannelOption_icon">
                            </view>
                            <view class="ChannelOption_channel-info">
                                <view class="ChannelOption_channel-label">
                                    <view class="ChannelOption_label">支付宝</view>
                                    <view class="ChannelOption_sub-label"></view>
                                    <view class="ChannelOption_check-option"></view>
                                </view>
                            </view>
                        </view>

                    </view>
                </view>


                <view class="payButtom">

                    <u-button type="warning"
                              text="确认支付V3" @click="toPay()" :disabled="payBtnDisabled"></u-button>
                </view>
            </section>
        </view>

        <u-loading-page
                :loadingText="loadingPageData.loadingText"
                :image="loadingPageData.image"
                :iconSize="loadingPageData.iconSize"
                :loadingMode="loadingPageData.loadingMode"
                :bgColor="loadingPageData.bgColor"
                :loading="loading"
                :color="loadingPageData.color"
                :loadingColor="loadingPageData.loadingColor"
        >
        </u-loading-page>

        <u-modal
                title="授权"
                :show="modal.show"
                style="text-align: center"
                :content="modal.content"
        >
            <view slot="default">
                <u-alert title="注意:请点击头像和昵称填充信息获取完整的微信支付服务!" type="warning"></u-alert>
                <button class="avatar-wrapper" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
                    <img class="avatar" :src="avatarUrl"/>
                </button>
                <!--            <input class="weui-input" type="nickname" name="nickname" @blur="changeName" placeholder="输入同步到view中"/>-->
                <!--            <u-input v-model="nickName" @change="changeName" type="nickname" placeholder="请输入昵称" />-->
                <!--            -->

                <input style="text-align: center;margin: 0 auto; padding: 0 0 30px 0;" @change="changeName" type="nickname" placeholder="请输入昵称"/>


                <u-button
                        v-if="avatarUrl != oldAvatarUrl && nickName.length > 0"
                        :showConfirmButton="false"
                        type="warning"
                        text="提交"
                        open-type="getUserInfo"
                        @tap="loginOrRegister"
                ></u-button>
            </view>

        </u-modal>


        <u-toast ref="uToast"></u-toast>
    </view>

</template>

<script>
import {jsapi, loginOrRegister} from "@/api/wechatPay";
import {productList} from '@/api/product'
import {toast} from "@/utils/common";

export default {
    data() {
        return {
            oldAvatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
            avatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
            nickName: '',
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


            title: 'Hello',
            modal: {
                show: false,
                content: '由于微信支付需要用户登录获取信息，请登录验证获取完整的微信支付服务',
            },
            payOrder: {
                productId: 1,
                payType: 'wxpay' // alipay or wxpay
            },
            productList: [], // 订单商品
            payBtnDisabled: false,

        }
    },
    onShow: function () {
        const storageSync = uni.getStorageSync('token');
        if ([null, undefined, ''].includes(storageSync)) {
            this.modal.show = true
        }
        console.log('App Show')
    },
    onLoad() {
        // 加载商品
        // 获取商品列表
        productList().then(response => {
            console.log(response.data);
            this.productList = response.data.productList
            this.payOrder.productId = this.productList[0].id
        })
    },
    methods: {
        // 设置头像
        onChooseAvatar(e) {
            console.log("onChooseAvatar", e);
            const {avatarUrl} = e.detail
            this.avatarUrl = avatarUrl
        },
        changeName(e) {
            console.log("changeName", e);
            console.log(e.detail.value);
            this.nickName = e.detail.value

        },
        // 授权登录获取OPENID
        /**
         * 登录或注册
         */
        loginOrRegister() {
            this.loading = true
            this.modal.show = false
            this.loadingPageData.loadingMode = 'spinner'
            this.loadingPageData.bgColor = 'rgba(0, 0, 0, 0.3)'
            this.loadingPageData.loadingText = "正在授权中请稍后..."
            this.loadingPageData.color = '#eee'
            this.loadingPageData.loadingColor = '#ddd'

            let yby6 = this
            // 获取微信用户的临时授权
            uni.login({
                provide: 'Weixin',
                success: function (res) {
                    yby6.code = res.code;
                }
            })
            // 获取用户信息
            this.getUserProfile();
        },
        /**
         * 获取用户信息
         */
        getUserProfile() {
            uni.getUserProfile({
                desc: '获取用户信息注册',
                success: (res) => {
                    this.requestLogin(res)
                }
            });
        },
        /**
         * 发送请求到后端登录
         * @param wxData 用户信息
         */
        requestLogin(wxData) {
            let info = wxData.userInfo
            info.nickName = this.nickName
            console.log("requestLogin", info);
            let data = {
                code: this.code,
                nickname: info.nickName
            }
            console.log(data);
            // 发送注册请求
            loginOrRegister(data).then(res => {
                let token = res.data.openId
                // 弹出信息
                let params = {
                    type: 'success',
                    title: '登录成功',
                    message: '登录成功',
                    iconUrl: 'https://cdn.uviewui.com/uview/demo/toast/success.png'
                }
                this.$refs.uToast.show({
                    ...params,
                    complete: () => {
                        // console.log("over....");
                        // 把token保存storage
                        uni.setStorageSync('token', token)
                        uni.setStorageSync('nickName', info.nickName)
                        this.modal.show = false
                        this.loading = false
                    }
                })
            })
        },
        /* 业务开始 */
        selectItem(productId) {
            console.log('商品id：' + productId)
            this.payOrder.productId = productId
            console.log(this.payOrder)
        },
        selectPayType(val) {
            this.payOrder.payType = val
        }
        ,
        toPay() {
            if (this.payOrder.payType === "alipay") {
                toast("支付宝证件过期")
                return
            }
            this.payBtnDisabled = true
            // 获取微信支付凭证创建支付订单
            const storageSync = uni.getStorageSync('token');
            const nickName = uni.getStorageSync('nickName');
            jsapi(
              this.payOrder.productId, storageSync + "|" + nickName
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
                        this.payBtnDisabled = false
                        // TODO 支付完毕 短轮询查询后台订单是否成功
                    },
                    fail: (res) => {
                        console.log(res);
                        toast("取消支付可继续点击支付重新发起")
                        this.payBtnDisabled = false
                    }
                })
            })
        }
        ,
    }
}
</script>

<style scoped lang="scss">
//.app-container {
//  /deep/ .u-modal__content__text {
//    text-align: center;
//  }
//}

.comp-wxpay-qrcode-tip {
  display: none;
}

.line {
  width: 300px;
  text-align: center;
  line-height: 30px;
  font-size: 12px;
  color: #fff;
  background-color: #00c800;
}

/deep/ .u-modal__button-group {
  display: none !important;
}

.avatar-wrapper {
  padding: 0;
  width: 56px !important;
  border-radius: 8px;
  margin-top: 40px;
  margin-bottom: 40px;
  background-color: #fff;
}

.avatar {
  display: block;
  width: 56px;
  height: 56px;
}

</style>
