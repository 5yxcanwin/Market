package com.hafa.market.util;

import com.hafa.market.enums.EnumWechatUrl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author heavytiger
 * @version 1.0
 * @description 访问wx提供的API接口，获取数据
 * @date 2022/4/19 14:36
 */
public class WxApiUtil {
    public static String code2session(String appid, String secret, String code, String grantType){
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("appid", appid);
        param.put("secret", secret);
        param.put("js_code", code);
        param.put("grant_type", grantType);
        return HttpUtil.doGet(EnumWechatUrl.JS_CODE_2_SESSION.getUrl(), param);
    }
}
