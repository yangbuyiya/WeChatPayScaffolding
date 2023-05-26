// yangbuyi Copyright (c) https://yby6.com 2023.

import vue from "@vitejs/plugin-vue";

import createAutoImport from "./auto-import";
import createSetupExtend from "./setup-extend";

export default function createVitePlugins(viteEnv, isBuild = false) {
  const vitePlugins = [vue({
    refTransform: true, reactivityTransform: true
  })];
  vitePlugins.push(createAutoImport());
  vitePlugins.push(createSetupExtend());
  return vitePlugins;
}
