package com.zyjclass.exceptions;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/30$
 */
public class ResponseException extends RuntimeException{
    private byte code;
    private String msg;
    public ResponseException(byte code,String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public ResponseException(Throwable cause){
        super(cause);
    }

    public ResponseException(String msg) {
        super(msg);
    }
}
