package codec;

import core.RpcRequest;
import lombok.Builder;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

@Data
@Builder
public class RequestBody implements Serializable {
    private static final long serialVersionUID = 1L;
    private String interfaceName;
    private String methodName;
    private Object[]params;
    private Class<?>[]paramTypes;

}
