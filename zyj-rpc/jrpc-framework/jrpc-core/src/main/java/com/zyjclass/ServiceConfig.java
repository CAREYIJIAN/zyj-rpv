package com.zyjclass;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
public class ServiceConfig<T> {

    private Class<T> interfaceProvider;
    private Object ref;

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Class<T> getInterface() {
        return interfaceProvider;
    }
}
