1.服务调用方 
    发送报文 writeAndFlush(object)  请求
    object应该是什么？
    JrpcRequest（
    1、请求id（long） 2、压缩类型（1byte） 3、序列化的方式（1byte） 
    4、消息类型（普通请求、心跳检测请求）（1byte）
    5、负载 payload（接口名字，方法的名字，参数列表，返回值类型）
    ）

    pipeline开始生效，报文开始出站
    ---> 第一个处理器（log处理日志）
    ---> 第二个处理器（编码器）（out）（转化 JrpcRequest -> msg（请求报文、序列化、压缩））

2.服务提供方
    通过netty接受请求报文
    pipeline开始生效，报文开始出站
    ---> 第一个处理器（log处理日志）
    ---> 第二个处理器（解码器）（out）（解压缩，反序列化，转化msg --> JrpcRequest）

3.执行方法调用，得到结果

4.服务提供方
    发送报文 writeAndFlush(object)  响应
    pipeline开始生效，报文开始出站
    ---> 第一个处理器（out）（转化 object -> msg（请求报文））
    ---> 第二个处理器（out）（序列化）
    ---> 第三个处理器（out）（压缩）

5.服务调用方
    通过netty接受响应报文
    pipeline开始生效，报文开始出站
    ---> 第一个处理器（in）（解压缩）
    ---> 第二个处理器（in）（反序列化）
    ---> 第三个处理器（in）（解析报文）

6.得到结果返回