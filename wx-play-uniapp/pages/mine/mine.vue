<!-- yangbuyi Copyright (c) https://yby6.com 2023. -->

<template>
    <view>
        <view class="top-container">

            <view class="info">
                <!--    点击微信会弹出授权弹窗     -->
                <view class="login-tip" open-type="getUserInfo" @tap="loginOrRegister">
                    点击登录
                </view>
                <button class="avatar-wrapper" open-type="chooseAvatar" bind:chooseavatar="onChooseAvatar">
                    <img class="avatar" :src="avatarUrl" />
                </button>
                <!-- <u-input type="nickname" class="weui-input" placeholder="请输入昵称"/> -->
            </view>
            <!--            <view class="right" v-if="flag == 'login'">-->
            <!--                <view>获取openID成功可以使用支付功能</view>-->
            <!--            </view>-->
        </view>
        <u-loading-page bg-color="#e8e8e8"></u-loading-page>
        <u-toast ref="uToast"></u-toast>
    </view>
</template>

<script>

import {loginOrRegister} from "@/api/wechatPay";

export default {
    data() {
        return {
            avatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
            loadingPageData: {
                // 自定义提示内容
                loadingText: '正在玩命加载中...',
                // 自定义图片
                image: '',
                // 自定义加载动画模式
                loadingMode: 'circle',
                // 字体颜色
                color: '#C8C8C8',
                loadingColor: '#C8C8C8',
                iconSize: 40
            },
            loading: false,
            flag: 'logout',
            code: null,
            labelStyle: {
                color: '#2074fd',
                'font-weight': 'bold'
            },
            iconStyle: {
                color: '#0764FD',
                'font-size': '34rpx',
                'margin-top': '3rpx'
            },
        };
    },
    methods: {
        onChooseAvatar(e) {
            const {avatarUrl} = e.detail
            this.setData({
                avatarUrl,
            })
        },
        /**
         * 登录或注册
         */
        loginOrRegister() {
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
            // uni.getUserProfile({
            // 	desc: '获取用户信息注册',
            // 	success: (res) => {
            // 		this.requestLogin(res)
            // 	}
            // });

            uni.getUserProfile({
                desc: '获取用户信息注册',
                success: (res) => {
                    console.log('getUserProfile', res)
                    this.requestLogin(res)
                }
            })
        },
        /**
         * 发送请求到后端登录
         * @param wxData 用户信息
         */
        requestLogin(wxData) {
            let info = wxData.userInfo
            console.log(info);
            let data = {
                code: this.code
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
                        // 更新页面标题
                        this.flag = 'login'

                    }
                })

                // 跳转到
                uni.switchTab({
                    url: "pages/index/index"
                })
            })

        },
        //
        // uni.showToast({
        // 	  //       icon: 'error',
        // 	  //       title: '数据不正确'
        // 	  //     })
    },
    onLoad() {
        console.log("onLoad");
        // 更新页面标题
        this.flag = uni.getStorageSync('token') ? 'login' : ''
    },

};
</script>

<style lang="scss">
.login-tip {
  font-size: 18px;
  margin-left: 10px;
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
