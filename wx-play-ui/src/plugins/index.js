// yangbuyi Copyright (c) https://yby6.com 2023.

import modal from "./modal";

export default function installPlugins(app) {
  // 模态框对象
  app.config.globalProperties.$modal = modal;
}
