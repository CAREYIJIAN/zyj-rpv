package com.zyjclass;


import com.zyjclass.compress.Compressor;
import com.zyjclass.compress.impl.GzipCompressor;
import com.zyjclass.discovery.RegistryConfig;
import com.zyjclass.loadbalancer.LoadBalancer;
import com.zyjclass.loadbalancer.impl.RoundRobinLoadBalancer;
import com.zyjclass.serialize.Serializer;
import com.zyjclass.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;


/**
 * 全局的配置类，代码配置 ———》xml配置———》spi配置———》默认项
 * @author CAREYIJIAN$
 * @date 2024/1/27$
 */
@Data
@Slf4j
public class Configuration {
    //端口号（配置信息）
    private int port = 8090;
    //序列化协议（配置信息）
    private String serializeType = "jdk";
    private Serializer serializer = new JdkSerializer();
    //压缩使用的协议（配置信息）
    private String compressType = "gzip";
    private Compressor compressor = new GzipCompressor();
    //应用程序的名字（配置信息）
    private String appName = "default";
    //配置的注册中心（配置信息）
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");
    //序列化协议（配置信息）
    private ProtocolConfig protocolConfig = new ProtocolConfig("jdk");
    //id生成器（配置信息）
    private IdGenerator idGenerator = new IdGenerator(1,2);
    //负载均衡策略（配置信息）
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    //读xml
    public Configuration(){
        //读取xml获得上边的信息
        loadFromXml(this);
    }

    /**
     * 从配置文件读取配置信息 使用dom4j或jdk自带的
     * @param configuration 配置实例
     */
    private void loadFromXml(Configuration configuration) {
        try {
            //1.创建一个document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("jrpc.xml");
            Document doc = builder.parse(inputStream);

            //2.获取一个xpath解析器
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            //3.解析所有标签
            configuration.setPort(resolvePort(doc, xPath));
            configuration.setAppName(resolveAppName(doc, xPath));
            configuration.setIdGenerator(resolveIdGenerator(doc, xPath));
            configuration.setRegistryConfig(resolveRegistryConfig(doc, xPath));
            configuration.setLoadBalancer(resolveLoadBalancer(doc, xPath));
            configuration.setSerializeType(resolveSerializeType(doc, xPath));
            configuration.setCompressType(resolveCompressType(doc, xPath));
            configuration.setCompressor(resolveCompressor(doc, xPath));
            configuration.setProtocolConfig(new ProtocolConfig(configuration.serializeType));
            configuration.setSerializer(resolveSerializer(doc, xPath));

            //如果有新增的标签这里继续修改

        }catch (ParserConfigurationException | SAXException | IOException  e){
            log.info("未发现相关配置文件或解析配置文件的时候发生了异常，将使用默认配置",e);
        }
    }

    /**
     * 解析序列化器
     * @param doc 文档
     * @param xPath 解析器
     * @return
     */
    private Serializer resolveSerializer(Document doc, XPath xPath) {
        String expression = "/configuration/serializer";
        Serializer serializer = parseObject(doc, xPath, expression, null);
        return serializer;
    }

    /**
     * 解析压缩器（具体实现）
     * @param doc 文档
     * @param xPath 解析器
     * @return
     */
    private Compressor resolveCompressor(Document doc, XPath xPath) {
        String expression = "/configuration/compressor";
        Compressor compressor = parseObject(doc, xPath, expression, null);
        return compressor;
    }

    /**
     * 解析压缩方式
     * @param doc 文档
     * @param xPath 解析器
     * @return
     */
    private String resolveCompressType(Document doc, XPath xPath) {
        String expression = "/configuration/compressType";
        String parseString = parseString(doc, xPath, expression, "type");
        return parseString;
    }

    /**
     * 解析序列化方式
     * @param doc 文档
     * @param xPath 解析器
     * @return
     */
    private String resolveSerializeType(Document doc, XPath xPath) {
        String expression = "/configuration/serializeType";
        String parseString = parseString(doc, xPath, expression, "type");
        return parseString;
    }

    /**
     * 解析负载均衡器
     * @param doc 文档
     * @param xPath 解析器
     * @return
     */
    private LoadBalancer resolveLoadBalancer(Document doc, XPath xPath) {
        String expression = "/configuration/loadBalancer";
        LoadBalancer loadBalancer = parseObject(doc, xPath, expression, null);
        return loadBalancer;
    }

    /**
     * 解析注册中心
     * @param doc 文档
     * @param xPath 解析器
     * @return
     */
    private RegistryConfig resolveRegistryConfig(Document doc, XPath xPath) {
        String expression = "/configuration/registry";
        String url = parseString(doc, xPath, expression,"url");
        return new RegistryConfig(url);
    }

    /**
     * 解析id生成器
     * @param doc 文档
     * @param xPath 解析器
     * @return
     */
    private IdGenerator resolveIdGenerator(Document doc, XPath xPath) {
        String expression = "/configuration/idGenerator";
        String clazz = parseString(doc, xPath, expression, "class");
        String dataCenterId = parseString(doc, xPath, expression, "dataCenterId");
        String MachineId = parseString(doc, xPath, expression, "MachineId");

        try {
            Class<?> aClass = Class.forName(clazz);
            IdGenerator instance =(IdGenerator) aClass.getConstructor(new Class[]{long.class, long.class})
                    .newInstance(Long.parseLong(dataCenterId), Long.parseLong(MachineId));
            return instance;
        }catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析应用名称
     * @param doc 文档
     * @param xPath 解析器
     * @return 应用名
     */
    private String resolveAppName(Document doc, XPath xPath) {
        String expression = "/configuration/appName";
        String parseString = parseString(doc, xPath, expression);
        return parseString;
    }

    /**
     * 解析端口号
     * @param doc 文档
     * @param xPath 解析器
     * @return 端口号
     */
    private int resolvePort(Document doc, XPath xPath) {
        String expression = "/configuration/port";
        String parseString = parseString(doc, xPath, expression);
        return Integer.parseInt(parseString);
    }

    /**
     * 解析一个节点，返回一个实例
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @param expression xpath表达式
     * @param paramType 参数列表
     * @param params 参数
     * @return 配置的实例
     * @param <T>
     */
    private <T> T parseObject(Document doc, XPath xPath, String expression ,Class<?>[] paramType, Object... params){
        try {
            XPathExpression expr = xPath.compile(expression);
            //我们的表达式可以帮助获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instant = null;
            if (paramType == null){
                instant = aClass.getConstructor().newInstance();
            }else {
                instant = aClass.getConstructor(paramType).newInstance(params);
            }
            return (T) instant;
        }catch (ClassNotFoundException | XPathExpressionException | NoSuchMethodException | InstantiationException |
                IllegalAccessException | InvocationTargetException e) {
            log.error("解析表达式时发生异常",e);
        }
        return null;
    }

    /**
     * 获取一个节点属性的值 <port num="777"/>
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @param expression xpath表达式
     * @param AttributeName 节点的相关属性名称(num)
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xPath, String expression, String AttributeName){
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem(AttributeName).getNodeValue();

        }catch (XPathExpressionException e) {
            log.error("解析表达式时发生异常",e);
        }
        return null;
    }

    /**
     * 获取一个节点文本 <port>7777</>
     * @param doc 文档对象
     * @param xPath xpath解析器
     * @param expression xpath表达式
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xPath, String expression){
        try {
            XPathExpression expr = xPath.compile(expression);
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getTextContent();

        }catch (XPathExpressionException e) {
            log.error("解析表达式时发生异常",e);
        }
        return null;
    }
}
