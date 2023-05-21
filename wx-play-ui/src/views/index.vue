<template>
  <div class="app-container">
    <div class="bg-fa of"   v-loading="loading">
      <section id="index" class="container">
        <header class="comm-title">
          <h2 class="fl tac">
            <span class="c-333">课程列表</span>
          </h2>
        </header>
        <div class="PaymentChannel_title"
             style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">
          本案例使用Native模式拉取二维码用户进行微信扫码支付指定商品操作。
        </div>
        <div class="PaymentChannel_title"
             style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">
          个人博客网站： <a href="https://yby6.com" target="_blank">https://yby6.com</a>
        </div>
        <div class="PaymentChannel_title"
             style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">
          博客项目案例： <a href="https://lzys522.cn" target="_blank">https://lzys522.cn</a>
        </div>
        <div class="PaymentChannel_title"
             style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">
          全网很火爱心案例： <a href="https://lzys522.cn/love/" target="_blank">https://lzys522.cn/love</a>
        </div>


        <ul>
          <li v-for="product in productList" :key="product.id">
            <a :class="['orderBtn', {current:payOrder.productId === product.id}]"
               @click="selectItem(product.id)"
               href="javascript:void(0);">
              {{ product.title }}
              ¥{{ product.price / 100 }}
            </a>
          </li>


        </ul>


        <div class="PaymentChannel_payment-channel-panel">
          <div class="PaymentChannel_title"
               style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">注意：
            0.5以上,支付成功后可在订单列表进行退款,此案例只是Demo走完全流程
          </div>

          <div class="PaymentChannel_title"
               style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">
            需要各大机构课程的私我Wechat：
            yangbuyiya
          </div>
          <h3 class="PaymentChannel_title">
              <span class="c-333">选择支付方式</span>
          </h3>
          <div class="PaymentChannel_channel-options">

            <!-- 选择微信 -->
            <div :class="['ChannelOption_payment-channel-option', {current:payOrder.payType === 'wxpay'}]"
                 @click="selectPayType('wxpay')">
              <div class="ChannelOption_channel-icon">
                <img src="../assets/img/wxpay.png" class="ChannelOption_icon">
              </div>
              <div class="ChannelOption_channel-info">
                <div class="ChannelOption_channel-label">
                  <div class="ChannelOption_label">微信支付</div>
                  <div class="ChannelOption_sub-label"></div>
                  <div class="ChannelOption_check-option"></div>
                </div>
              </div>
            </div>
            {{ payType }}
            <!-- 选择支付宝 -->
            <div :class="['ChannelOption_payment-channel-option', {current:payOrder.payType === 'alipay'}]"
                 @click="selectPayType('alipay')">
              <div class="ChannelOption_channel-icon">
                <img src="../assets/img/alipay.png" class="ChannelOption_icon">
              </div>
              <div class="ChannelOption_channel-info">
                <div class="ChannelOption_channel-label">
                  <div class="ChannelOption_label">支付宝</div>
                  <div class="ChannelOption_sub-label"></div>
                  <div class="ChannelOption_check-option"></div>
                </div>
              </div>
            </div>

          </div>
        </div>

        <div class="payButtom">
          <el-button
              :disabled="payBtnDisabled"
              type="warning"
              round
              style="width: 180px;height: 44px;font-size: 18px;"
              @click="toPay()">
            确认支付V3
          </el-button>
          <el-button
              :disabled="payBtnDisabled"
              type="warning"
              round
              style="width: 180px;height: 44px;font-size: 18px;"
              @click="toPayV2()">
            确认支付V2
          </el-button>
        </div>
      </section>
      <!-- 微信支付二维码 -->
      <el-dialog
          v-model="codeDialogVisible"
          :width="widthBox"
          center>
        <div style="display: flex">
          <div>
            <qrcode-vue :value="codeUrl" :size="300" level="H"/>
            <div class="line">请打开手机微信，扫一扫完成支付</div>
          </div>

          <div class="comp-wxpay-qrcode-tip"><img
              src="https://qiniu.staticfile.org/static/wxpay_tip-ac45fc3d17795a22330c.png" title=""
              style="">
          </div>

        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import productApi from '../api/product.js'
import wxPayApi from '../api/wxPay.js'
import orderInfoApi from '../api/orderInfo.js'
import QrcodeVue from "qrcode.vue";
import {getCurrentInstance, onBeforeMount, ref} from "vue";

const {proxy} = getCurrentInstance();
// 确认支付按钮是否禁用
let payBtnDisabled = ref(false)
// 微信支付二维码弹出
let codeDialogVisible = ref(false)
// 商品列表
const productList = ref([])
// 订单信息
const payOrder = ref({
  productId: 1, //商品id
  payType: 'wxpay' //支付方式
})

let widthBox = ref('350px')
let codeUrl = ref('')
let orderNo = ref('')
let timer = ref(null)
let loading = ref(false)

//选择商品
function selectItem(productId) {
  console.log('商品id：' + productId)
  payOrder.value.productId = productId
  console.log(payOrder)
  //$router.push({ path: '/order' })
}

//选择支付方式
const selectPayType = (type) => {
  payOrder.value.payType = type
  console.log('支付方式：', payOrder.value)

  //$router.push({ path: '/order' })
}

//确认支付
function toPay() {
  //禁用按钮，防止重复提交
  payBtnDisabled = true

  //微信支付
  if (payOrder.value.payType === 'wxpay') {
    //调用统一下单接口
    wxPayApi.nativePay(payOrder.value.productId).then(response => {
      console.log(response);
      codeUrl.value = response.data.codeUrl
      orderNo.value = response.data.orderNo

      //打开二维码弹窗
      codeDialogVisible.value = true

      //启动定时器
      timer.value = setInterval(() => {
        //查询订单是否支付成功
        queryOrderStatus()
      }, 1000)

    })
  } else {
    proxy.$message.error("支付宝没搞")
    payBtnDisabled = false
  }
}

//确认支付
function toPayV2() {
  //禁用按钮，防止重复提交
  payBtnDisabled = true

  //微信支付
  if (payOrder.value.payType === 'wxpay') {
    //调用统一下单接口
    wxPayApi.nativePayV2(payOrder.value.productId).then(response => {
      codeUrl.value = response.data.codeUrl
      orderNo.value = response.data.orderNo

      //打开二维码弹窗
      codeDialogVisible.value = true


      //启动定时器
      timer.value = setInterval(() => {
        //查询订单是否支付成功
        queryOrderStatus()
      }, 1000)


    })
  } else {
    proxy.$message.error("支付宝没搞")
    payBtnDisabled = false
  }
}

//关闭微信支付二维码对话框时让“确认支付”按钮可用
function closeDialog() {
  console.log('close.................')
  payBtnDisabled = false
  console.log('清除定时器')
  clearInterval(timer.value)
}


// 查询订单状态
function queryOrderStatus() {

  orderInfoApi.queryOrderStatus(orderNo.value).then(response => {
    console.log('查询订单状态：' + response.code)

    // 支付成功后的页面跳转
    if (response.code === 0) {
      closeDialog()
      proxy.$notify.success({
        message: "支付成功"
      });
      // 三秒后跳转到订单列表
      setTimeout(() => {
        // 刷新页面
        location.reload()
      }, 1500)
    }
  })
}

// 自定义课程金额
function open() {
  // $prompt('请输入自定义金额', '提示', {
  //   confirmButtonText: '确定',
  //   cancelButtonText: '取消',
  // }).then(({value}) => {
  // }).catch(() => {
  //   $message({
  //     type: 'info',
  //     message: '取消输入'
  //   });
  // });
}

onBeforeMount(() => {
  widthBox.value = window.innerWidth <= 500 ? '350px' : '650px'
	loading.value = true
  //获取商品列表
  productApi.list().then(response => {
    console.log(response.data);
    productList.value = response.data.productList
    payOrder.value.productId = productList.value[0].id
	  loading.value = false
  })

})

</script>

<style scoped>
.of {
  overflow: hidden;
  width: 1200px;
  margin: 0 auto;
}

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

@media screen and (min-width: 1000px) {
  .comp-wxpay-qrcode-tip {
    display: block;
    margin-left: 40px;
    margin-top: 5px;
  }


  .comp-wxpay-qrcode-tip > img {
    width: 240px;
    height: 300px;
  }


}


/* 在小于或等于 992 像素的屏幕上，设置单独css */
@media screen and (max-width: 500px) {
  .of {
    overflow: hidden;
    width: 100%;
    margin: 0 auto;
  }

  #index ul {
    list-style-type: none;
    padding: 0 !important;
    margin: 0 auto;
    text-align: center;
  }

  #index a.orderBtn {
    color: #f39800;
    border-radius: 5px;
    width: 158px;
    height: 60px;
    line-height: 60px;
    font-size: 17px;
    display: inline-block;
    text-align: center;
    text-decoration: none;
  }

  .payButtom {
    width: 100%;
    margin: 0 auto;
    text-align: center;
  }

  .el-button.is-round {
    margin-top: 10px;
    margin-left: 0;
  }

  .container {
    margin-left: auto;
    margin-right: auto;
    width: 100%;
    padding: 7px;
    box-sizing: border-box;
  }
}

</style>
