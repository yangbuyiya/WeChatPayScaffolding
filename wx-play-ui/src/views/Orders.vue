<!-- yangbuyi Copyright (c) https://yby6.com 2023. -->

<template>
  <div class="app-container">
    <div class="bg-fa of">
      <section id="index" class="container">
        <header class="comm-title">
          <h2 class="fl tac">
            <span class="c-333">订单列表</span>
          </h2>
        </header>
        <div class="PaymentChannel_title"
             style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">注意：
          0.5以上,支付成功后可在订单列表进行退款,此案例只是Demo走完全流程
        </div>
        <div class="PaymentChannel_title"
             style="color: red;  margin-bottom: 10px;font-size: 14px;height: auto !important;">
          订单下单30分钟之内未支付的订单进行超时处理。
        </div>
        <el-table :data="list" border style="width: 100%;height:600px">
          <el-table-column v-if="!widthBox" type="index" width="50"/>
          <el-table-column v-if="!widthBox" prop="orderNo" label="订单编号" width="230"/>
          <el-table-column prop="title" label="订单标题" align="center"/>
          <el-table-column prop="totalFee" label="订单金额" align="center">
            <template #default="scope">
              {{ scope.row.totalFee / 100 }} 元
            </template>
          </el-table-column>
          <el-table-column label="订单状态" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.orderStatus === '未支付'">
                {{ scope.row.orderStatus }}
              </el-tag>
              <el-tag v-if="scope.row.orderStatus === '支付成功'" type="success">
                {{ scope.row.orderStatus }}
              </el-tag>
              <el-tag v-if="scope.row.orderStatus === '超时已关闭'" type="warning">
                {{ scope.row.orderStatus }}
              </el-tag>
              <el-tag v-if="scope.row.orderStatus === '用户已取消'" type="info">
                {{ scope.row.orderStatus }}
              </el-tag>
              <el-tag v-if="scope.row.orderStatus === '退款中'" type="danger">
                {{ scope.row.orderStatus }}
              </el-tag>
              <el-tag v-if="scope.row.orderStatus === '已退款'" type="info">
                {{ scope.row.orderStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" align="center"/>
          <el-table-column label="操作" width="70" align="center">
            <template #default="scope">
              <el-button v-if="scope.row.orderStatus === '未支付'" type="text" @click="cancel(scope.row.orderNo)">取消
              </el-button>
              <el-button v-if="scope.row.orderStatus === '支付成功'" type="text"
                         @click="refund(scope.row.orderNo, scope.row.totalFee)">退款
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <!-- 退款对话框 -->
      <el-dialog
          title="退款"
          v-model="refundDialogVisible"
          @close="closeDialog"
          width="350px"
          center>
        <el-form>
          <el-form-item label="交易订单号">
            <el-input style="width: 100%;" v-model="refundNo" placeholder="请输入交易单号后四位"></el-input>
          </el-form-item>
          <el-form-item label="退款原因">
            <el-select style="width: 100%;" v-model="reason" placeholder="请选择退款原因">
              <el-option label="不喜欢" value="不喜欢"></el-option>
              <el-option label="买错了" value="买错了"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="toRefunds()" :disabled="refundSubmitBtnDisabled">确 定</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import orderInfoApi from "../api/orderInfo"
import wxPayApi from "../api/wxPay"

export default {
  data() {
    return {
      list: [], //订单列表
      refundDialogVisible: false, //退款弹窗
      orderNo: '', //退款订单号
      reason: '', //退款原因,
      refundNo: '', // 交易单号
      refundSubmitBtnDisabled: false, //防止重复提交
      widthBox: false // 是否移动端
    };
  },

  created() {
    this.showOrderList()
    this.widthBox = window.innerWidth <= 500
  },

  methods: {

    //显示订单列表
    showOrderList() {
      orderInfoApi.list().then((response) => {
        this.list = response.data.list;
      });
    },

    //用户取消订单
    cancel(orderNo) {
      wxPayApi.cancel(orderNo).then(response => {
        this.$message.success({message: response.message})
        //刷新订单列表
        this.showOrderList()
      })
    },

    //退款对话框
    refund(orderNo, total, refundNo) {
      if ((total / 100) < 0.5) {
        this.$message.success({
          message: '金额太少就不退了,感谢老板的支持!'
        })
        return;
      }

      this.refundDialogVisible = true
      this.orderNo = orderNo
      this.refundNo = refundNo
    },

    //关闭退款对话框
    closeDialog() {
      console.log('close.................')
      this.refundDialogVisible = false
      //还原组件状态
      this.orderNo = ''
      this.reason = ''
      this.refundSubmitBtnDisabled = false
    },

    //确认退款
    toRefunds() {
      if ([undefined, null, ''].includes(this.refundNo) || this.refundNo.length < 4) {
        this.$notify.error({
          title: '错误',
          message: '请输入四位交易订单号'
        });
        return
      }
      this.refundSubmitBtnDisabled = true //禁用按钮，防止重复提交
      wxPayApi.refunds(this.orderNo, this.reason, this.refundNo).then(response => {
        console.log('response', response)
        this.closeDialog()
        this.showOrderList()
      }).catch(err => {
        console.log(err);
        this.refundSubmitBtnDisabled = false
      })
    }
  }
};
</script>
<style scoped>
@media screen and (max-width: 500px) {
  .container{
    padding: 0 25px;
  }
}
</style>