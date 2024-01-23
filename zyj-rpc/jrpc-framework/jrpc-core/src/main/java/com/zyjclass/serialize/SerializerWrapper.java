package com.zyjclass.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerializerWrapper {
    private byte code;
    private String type;
    private Serializer serializer;
}
