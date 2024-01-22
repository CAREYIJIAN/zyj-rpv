package com.zyjclass.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用来描述,请求调用方,所请求的,接口方法的描述
 * @author CAREYIJIAN$
 * @date 2024/1/20$
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPayload implements Serializable {

    //1.接口的名字
    private String interfaceName;
    //2.调用的方法名字
    private String methodName;
    //3.参数列表（参数类型（确定重载的方法）、具体参数（方法具体调用））
    private Class<?>[] parametsType;
    private Object[] parametersValue;
    //4.返回值的封装
    private Class<?> returnType;



}
