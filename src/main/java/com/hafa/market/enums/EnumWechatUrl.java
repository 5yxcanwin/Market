package com.hafa.market.enums;

/**
 * @author heavytiger
 * @version 1.0
 * @description 微信相关的API
 * @date 2022/4/19 14:45
 */
public enum EnumWechatUrl {
    JS_CODE_2_SESSION("https://api.weixin.qq.com/sns/jscode2session");

    private String url;

    EnumWechatUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
