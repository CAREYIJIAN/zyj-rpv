package com.zyjclass;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
public class ServiceConfig<T> {

    private Class<?> interfaceProvider;
    private Object ref;

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Class<?> getInterface() {
        return interfaceProvider;
    }
}
