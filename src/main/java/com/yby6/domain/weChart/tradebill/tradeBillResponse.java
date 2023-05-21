package com.yby6.domain.weChart.tradebill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yang shuai
 * @date 2022/11/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class tradeBillResponse{

    /**
     * 下载地址
     * 供下一步请求账单文件的下载地址，该地址30s内有效。
     */
    private String download_url;
    /**
     * 哈希类型
     * SHA1：SHA1值
     */
    private String hash_type;
    /**
     * 哈希值
     * 原始账单（gzip需要解压缩）的摘要值，用于校验文件的完整性。
     */
    private String hash_value;

}
