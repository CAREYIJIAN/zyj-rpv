package com.zyjclass;

import com.zyjclass.annotation.JrpcService;
import com.zyjclass.proxy.ProxyFactory;
import com.zyjclass.transport.message.JrpcResponse;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author CAREYIJIAN$
 * @date 2024/2/1$
 */
@Component
public class JrpcProxyBeanPostProcessor implements BeanPostProcessor {
    //会拦截所有的bean的创建，会在每一个bean初始化后被调用
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        //想办法给他生成一个代理
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields){
            JrpcService jrpcService = field.getAnnotation(JrpcService.class);
            if (jrpcService != null){
                //获取一个代理
                Class<?> type = field.getType();
                Object proxy = ProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
