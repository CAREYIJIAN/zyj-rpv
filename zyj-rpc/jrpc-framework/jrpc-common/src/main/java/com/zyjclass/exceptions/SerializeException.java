package com.zyjclass.exceptions;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
public class SerializeException extends RuntimeException{

    public SerializeException(){

    }

    public SerializeException(String message){
        super(message);
    }

    public SerializeException(Throwable cause){
        super(cause);
    }


}
