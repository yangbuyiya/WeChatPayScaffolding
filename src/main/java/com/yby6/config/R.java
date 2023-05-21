package com.yby6.config;

import com.yby6.domain.TProduct;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 返回响应
 *
 * @author yang shuai
 * @date 2022/11/13
 * <p>
 * Accessors链式设置参数
 * </p>
 */
@Data
@Accessors(chain = true)
public class R {
    private Integer code;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    public static R ok() {
        R r = new R();
        r.setCode(0);
        r.setMessage("成功");
        return r;
    }

    public static R error() {
        R r = new R();
        r.setCode(-1);
        r.setMessage("失败");
        return r;
    }

    public static R error(String err) {
        R r = new R();
        r.setCode(-1);
        r.setMessage(err);
        return r;
    }

    public R data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

}
