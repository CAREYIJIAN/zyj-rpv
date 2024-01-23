package com.zyjclass.enumeration;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/22$
 */
public enum RespCode {
    SUCCESS((byte) 1, "成功"),FAIL((byte) 2,"失败");
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
