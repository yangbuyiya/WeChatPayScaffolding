// yangbuyi Copyright (c) https://yby6.com 2023.

import App from "./App";
import uView from "@/uni_modules/uview-ui";
import Vue from "vue";

Vue.config.productionTip = false;
App.mpType = "app";
Vue.use(uView);
const app = new Vue({
  ...App,
});

app.$mount();
