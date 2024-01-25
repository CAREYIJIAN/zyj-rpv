package com.zyjclass.exceptions;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/24$
 */
public class LoadBalancerException extends RuntimeException{

    public LoadBalancerException(){
    }
    public LoadBalancerException(Throwable e){
        super(e);
    }

    public LoadBalancerException(String msg){
        super(msg);
    }
}
