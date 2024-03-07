package com.zyjclass.exceptions;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/18$
 */
public class DiscoveryException extends RuntimeException{

    public DiscoveryException(){

    }
    public DiscoveryException(Throwable cause){
        super(cause);
    }

    public DiscoveryException(String msg) {
        super(msg);
    }
}
