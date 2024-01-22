package com.zyjclass.enumeration;

/**
 * 用来标记请求类型
 * @author CAREYIJIAN$
 * @date 2024/1/20$
 */
public enum RequestType {

    REQUEST((byte)1,"普通请求"),HERT_BEAT((byte)2,"心跳检测");

    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId(){
        return id;
    }

    public String getType(){
        return type;
    }

}
