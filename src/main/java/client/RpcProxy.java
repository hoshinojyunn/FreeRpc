package client;

import codec.RequestBody;
import codec.ResponseBody;
import com.sun.istack.internal.Nullable;
import core.RpcBuilder;
import core.RpcConstants;
import core.RpcRequest;
import core.RpcResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import static core.RpcConstants.*;

public class RpcProxy implements InvocationHandler {
    private String registerHost = "localhost";
    private int registerPort = 9000;

    public <T> T getProxy(Class<T>target) {
        return (T)Proxy.newProxyInstance(
                target.getClassLoader(),
                new Class<?>[]{target},
                this);
    }

    @Override
    public Object invoke(@Nullable Object proxy, Method method, Object[] args) throws Throwable {
        // 构造请求体
        RequestBody requestBody = RequestBody.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();

        //向注册中心查询服务
        RpcRequest rpcRequest = RpcBuilder.buildRpcRequest(RpcBuilder.buildQueryHeaders(), requestBody);
        // 发送到服务注册中心查找需要的信息
        RpcTransfer rpcClient = new RpcTransfer();

        RpcResponse rpcResponse = rpcClient.sendRequest(rpcRequest, registerHost, registerPort);
        // 获取返回体中的结果
        if(rpcResponse.getHeaders().get(VERSION_KEY).equals(VERSION)){

            // 得到注册中心返回的结果
            ResponseBody responseBody = RpcBuilder.getResponseBody(rpcResponse);
            // 注册中心改写好的请求
            RpcRequest fixedRequest = (RpcRequest)responseBody.getResult();

            // 发到服务器
            RpcResponse serverResponse = rpcClient.sendRequest(fixedRequest,
                    fixedRequest.getHeaders().get(REMOTE_HOST),
                    Integer.parseInt(fixedRequest.getHeaders().get(REMOTE_PORT)));

            ResponseBody serverResponseBody = RpcBuilder.getResponseBody(serverResponse);
            if(!VERSION.equals(serverResponse.getHeaders().get(VERSION_KEY)))
                System.out.println("版本不一致");
            return serverResponseBody.getResult();

        }

        return null;
    }

    public String getRegisterHost() {
        return registerHost;
    }

    public void setRegisterHost(String registerHost) {
        this.registerHost = registerHost;
    }

    public int getRegisterPort() {
        return registerPort;
    }

    public void setRegisterPort(int registerPort) {
        this.registerPort = registerPort;
    }
}
