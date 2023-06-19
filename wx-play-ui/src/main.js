// yangbuyi Copyright (c) https://yby6.com 2023.

// main.ts
import { createApp } from "vue";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import "./assets/css/index.scss"; // global css
import "./styles/index.scss";
import App from "./App.vue";
// 引入路由器
import router from "./router";
// 二维码生成器
import QrcodeVue from "qrcode.vue";
// 注册指令
import plugins from "./plugins"; // plugins

const app = createApp(App);

app.use(ElementPlus);
app.use(router);
app.use(plugins);
app.use(QrcodeVue);

app.mount("#app");
