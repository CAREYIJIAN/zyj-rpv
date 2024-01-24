package com.zyjclass.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompressorWrapper {
    private byte code;
    private String type;
    private Compressor compressor;


}
