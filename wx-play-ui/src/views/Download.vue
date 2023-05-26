<!-- yangbuyi Copyright (c) https://yby6.com 2023. -->

<template>
  <div class="bg-fa of">
    <section id="index" class="container">
      <header class="comm-title">
        <h2 class="fl tac">
          <span class="c-333">账单申请</span>
        </h2>
      </header>
      <div class="PaymentChannel_title"
           style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">
        注意： 由于账单存在敏感信息，此处不进行演示.
      </div>
      <el-form :inline="true" >
        <el-form-item>
            <el-date-picker
                v-model="billDate"
                type="date"
                placeholder="选择账单日期"
                value-format="YYYY-MM-DD"
            />
        </el-form-item>
        <el-form-item>
            <el-button type="primary" @click="downloadBill('tradebill')">下载交易账单</el-button>
        </el-form-item>
         <el-form-item>
            <el-button type="primary" @click="downloadBill('fundflowbill')">下载资金账单</el-button>
        </el-form-item>
      </el-form>
    </section>

  </div>
</template>

<script setup lang="ts">
import billApi from '../api/bill.js'
import {getCurrentInstance, ref} from "vue";

const { proxy } = getCurrentInstance();

// 账单日期
let billDate = ref<String>('');

// 下载账单
function downloadBill(type) {
    if (type) {
        // 不给你玩
        proxy.$msgbox({ type: 'info', message: '不给你玩' });
        return;
    }
    //获取账单内容
    billApi.downloadBill(billDate.value, type).then(response => {
        console.log(response)
        const element = document.createElement('a')
        element.setAttribute('href', 'data:application/vnd.ms-excel;charset=utf-8,' + encodeURIComponent(response.data.result))
        element.setAttribute('download', billDate.value + '-' + type)
        element.style.display = 'none'
        element.click()
    })
}
</script>
