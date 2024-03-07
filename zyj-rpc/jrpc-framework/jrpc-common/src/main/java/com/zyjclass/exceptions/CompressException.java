package com.zyjclass.exceptions;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
public class CompressException extends RuntimeException{

    public CompressException(){

    }

    public CompressException(String message){
        super(message);
    }

    public CompressException(Throwable cause){
        super(cause);
    }


}
