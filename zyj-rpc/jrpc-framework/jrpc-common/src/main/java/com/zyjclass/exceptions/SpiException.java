package com.zyjclass.exceptions;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/28$
 */
public class SpiException extends RuntimeException {
    public SpiException(){

    }

    public SpiException(String message){
        super(message);
    }

    public SpiException(Throwable cause){
        super(cause);
    }


}
