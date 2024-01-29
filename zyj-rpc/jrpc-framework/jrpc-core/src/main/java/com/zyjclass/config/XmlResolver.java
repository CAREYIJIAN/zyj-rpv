package com.zyjclass.config;

import com.zyjclass.IdGenerator;
import com.zyjclass.ProtocolConfig;
import com.zyjclass.compress.Compressor;
import com.zyjclass.compress.CompressorFactory;
import com.zyjclass.discovery.RegistryConfig;
import com.zyjclass.loadbalancer.LoadBalancer;
import com.zyjclass.serialize.Serializer;
import com.zyjclass.serialize.SerializerFactory;
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
import java.util.Objects;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/28$
 */
@Slf4j
public class XmlResolver {


    /**
     * 从配置文件读取配置信息 使用dom4j或jdk自带的
     * @param configuration 配置实例
     */
    public void loadFromXml(Configuration configuration) {
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
            //配置新的压缩和序列化方式，并将其纳入工厂中
            ObjectWrapper<Serializer> serializerObjectWrapper = resolveSerializer(doc, xPath);
            SerializerFactory.addSerializer(serializerObjectWrapper);
            ObjectWrapper<Compressor> compressorObjectWrapper = resolveCompressor(doc, xPath);
            CompressorFactory.addCompressor(compressorObjectWrapper);


            //如果有新增的标签这里继续修改

        }catch (ParserConfigurationException | SAXException | IOException e){
            log.info("未发现相关配置文件或解析配置文件的时候发生了异常，将使用默认配置",e);
        }
    }

    /**
     * 解析序列化器
     * @param doc 文档
     * @param xPath 解析器
     * @return
     */
    private ObjectWrapper<Serializer> resolveSerializer(Document doc, XPath xPath) {
        String expression = "/configuration/serializer";
        Serializer serializer = parseObject(doc, xPath, expression, null);
        byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xPath, expression, "code")));
        String type = parseString(doc, xPath, expression,"type");
        return new ObjectWrapper(code,type,serializer);
    }

    /**
     * 解析压缩器（具体实现）
     * @param doc 文档
     * @param xPath 解析器
     * @return ObjectWrapper<Compressor>
     */
    private ObjectWrapper<Compressor> resolveCompressor(Document doc, XPath xPath) {
        String expression = "/configuration/compressor";
        Compressor compressor = parseObject(doc, xPath, expression, null);
        byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xPath, expression, "code")));
        String type = parseString(doc, xPath, expression,"type");
        return new ObjectWrapper(code,type,compressor);
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
