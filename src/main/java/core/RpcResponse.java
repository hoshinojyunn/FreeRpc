package core;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

@Data
@Builder
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, String> headers;
    private byte[]body;
}
