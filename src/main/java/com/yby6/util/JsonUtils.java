// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {

    static ObjectMapper mapper = new ObjectMapper();

    public static String toJsonStr(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static <T> T toObject(String jsonStr, Class<T> clazz) {

        try {
            return mapper.readValue(jsonStr, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject2(String jsonStr, TypeReference<T> type) {

        try {
            return mapper.readValue(jsonStr, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
