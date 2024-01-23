package com.zyjclass.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务提供方回复的响应
 * @author CAREYIJIAN$
 * @date 2024/1/22$
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JrpcResponse {
    //响应码 1成功 2异常
    private byte code;

    //请求的id
    private long requestId;
    //压缩的类型
    private byte compressType;
    //序列化的方式
    private byte serializeType;
    //消息体
    private Object body;

}
