// yangbuyi Copyright (c) https://yby6.com 2023.

import autoImport from "unplugin-auto-import/vite";

export default function createAutoImport() {
  return autoImport({
    imports: ["vue", "vue-router"], dts: false
  });
}
