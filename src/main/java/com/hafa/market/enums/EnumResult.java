package com.hafa.market.enums;

/**
 * @author heavytiger
 * @version 1.0
 * @description 状态枚举类，用来标识状态码和消息的对应关系
 * @date 2022/4/16 21:10
 */
public enum EnumResult {
    SESSION_SUCCESS(200200,"获取JSESSIONID成功"),
    UPDATE_SUCCESS(200201,"信息修改成功"),
    UPLOAD_SUCCESS(200202,"信息上传成功"),
    QUERY_SUCCESS(200203,"查询成功"),
    DELETE_SUCCESS(200204,"删除成功"),
    FAIL(400400,"请求失败"),
    TIME_OUT(400401,"超时"),
    NOT_FOUND(400404,"不存在"),
    INTERNAL_SERVER_ERROR(500500,"服务器内部错误"),
    SESSION_ERROR(500501,"JSESSIONID鉴权失败"),
    SESSION_RESET(500502, "JSESSIONID未过期，已重设过期时间"),
    LOGIN_ERROR(500503, "登录失败，服务器可能故障"),
    ARTICLE_ERROR(500504, "用户与产品页对应失败"),
    PICTYPE_ERROR(500505, "图片为空或类型错误"),
    OVER_MAX_SIZE(500506, "单个文件大小不能超过5MB"),
    ALL_OVER_MAX_SIZE(500507, "单次上传所有文件总和不能超过45MB"),
    UPDATE_ERROR(500508,"更新失败"),
    QUERY_ERROR(500509,"商品不存在"),
    DELETE_ERROR(500510,"删除失败"),
    UPLOAD_ERROR(500511,"信息上传失败"),
    WANTBUY_ERROR(500512,"操作失败"),
    QUERY_PURCHASE_ERROR(500513,"没有想要购买的");


    private int code;
    private String msg;

    EnumResult() {
    }

    EnumResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "EnumResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
