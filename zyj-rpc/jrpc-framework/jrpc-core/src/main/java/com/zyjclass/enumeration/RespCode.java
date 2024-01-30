package com.zyjclass.enumeration;

/**
 * 响应码:
 * 成功码 20（方法成功调用） 21（心跳成功返回）
 * 错误码 50（服务端方法调用失败）
 * 错误码 44（客户端失败）
 * 负载码 31（服务器负载过高，被限流）
 * @author CAREYIJIAN$
 * @date 2024/1/22$
 */
public enum RespCode {
    SUCCESS((byte) 20, "方法成功调用"),
    SUCCESS_HEART_BEAT((byte) 21,"心跳检测成功返回"),
    RATE_LIMIT((byte) 31,"服务端被限流"),
    RESOURCE_NOT_FOUND((byte) 44,"请求的资源不存在"),
    FAIL((byte) 50,"服务端方法调用失败");
    private byte code;
    private String desc;

    RespCode(byte code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
