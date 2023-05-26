// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6.domain.weChatJSAPi;

import lombok.Data;

/**
 * @author yang shuai
 * @date 2023/2/18
 */
@Data
public class LoginUser {
    /**
     * 小程序传递授权code
     */
    String code;
    /**
     * 微信名称
     */
    String nickname;
    /**
     * 微信头像
     */
    String photo;

    /**
     * 微信唯一ID
     */
    String openId;
}
