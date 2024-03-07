package com.zyjclass.config;

import com.zyjclass.serialize.Serializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/28$
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectWrapper<T> {
    private byte code;
    private String type;
    private T impl;

}
