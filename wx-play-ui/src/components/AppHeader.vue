<!-- yangbuyi Copyright (c) https://yby6.com 2023. -->

<template>
  <!-- 公共头 -->
  <header id="header">
    <section class="container">
      <ul class="nav">
        <router-link to="/wx/" tag="li" active-class="current" exact>
          <a>购买课程</a>
        </router-link>
        <router-link to="/wx/orders" tag="li" active-class="current">
          <a>我的订单</a>
        </router-link>
        <router-link to="/wx/download" tag="li" active-class="current">
          <a>下载账单</a>
        </router-link>
      </ul>
      <div class="clear"></div>
      <div class="toggleDarkBtn">
        <div style="text-align: right">
          <el-button
            style="text-align: right"
            color="#626aef"
            :dark="isDark"
            @click="toggleDarkBtn()"
          >
            {{ toggleTitle }}
          </el-button>
        </div>
      </div>
    </section>
  </header>
</template>
<script setup>
import { isDark, toggleDark } from "@/composables";
import { ref } from "vue";

let toggleTitle = ref("明亮");
const toggleDarkBtn = () => {
  toggleTitle = toggleDark() ? "明亮" : "暗色";
};
</script>
<style scoped>
.c-master {
  color: #68cb9b;
}

.bg-orange {
  background-color: #68cb9b;
}

.nav,
.nav,
.nav a {
  float: left;
}

.nav {
  padding: 15px 0 0 50px;
}

.nav,
.nav a {
  float: left;
}

.nav a {
  border-bottom: 2px solid transparent;
  color: #666;
  display: block;
  font-size: 20px;
  line-height: 50px;
  padding: 0 10px;
  margin: 0 10px;
  transition: 0.3s;
  -webkit-transition: 0.3s;
}

.nav a:hover,
.nav a {
  text-decoration: none;
}

.nav > .current a {
  border-color: #68cb9b;
  color: #68cb9b;
}

a {
  border-color: #68cb9b;
  color: #68cb9b;
}

.nav a {
  border-bottom: 2px solid transparent;
  color: #666;
  display: block;
  font-size: 20px;
  line-height: 50px;
  padding: 0 10px;
  margin: 0 10px;
  transition: 0.3s;
  -webkit-transition: 0.3s;
}

.toggleDarkBtn {
  margin-right: 60px;
}
@media screen and (max-width: 500px) {
  .toggleDarkBtn {
    margin-top: 15px;
    /*width: 30%;*/
    margin-right: 20px;
  }

  .nav,
  .nav a {
    display: flex;
    justify-content: center;
    flex-wrap: nowrap;
    flex-direction: row;
    padding: 0;
    margin-left: 10px;
  }

  .container {
    padding: 0;
  }
}
</style>
