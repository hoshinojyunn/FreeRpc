package codec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class ResponseBody implements Serializable {
    private static final long serialVersionUID = 1L;
    private Object result;
}
