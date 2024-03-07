package com.zyjclass.exceptions;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/18$
 */
public class NetworkException extends RuntimeException{
    public NetworkException(){

    }

    public NetworkException(Throwable cause){
        super(cause);
    }

    public NetworkException(String msg) {
        super(msg);
    }
}
