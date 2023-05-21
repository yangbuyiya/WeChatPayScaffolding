package com.yby6.util;

import com.google.gson.Gson;

/**
 * 谷歌 序列化
 * @author yang shuai
 * @date 2022/12/22
 */
public class GsonUtils {

    static Gson gson = new Gson();

    public static String toJsonStr(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

}
