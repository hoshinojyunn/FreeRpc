package server;

import codec.RequestBody;
import codec.ResponseBody;
import core.RpcBuilder;
import core.RpcRequest;
import core.RpcResponse;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import static core.RpcConstants.*;

public class RpcServerTask implements Runnable{
    private Socket clientSocket;

    public RpcServerTask(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            RpcRequest rpcRequest = (RpcRequest)ois.readObject();
            if(rpcRequest.getHeaders().get(VERSION_KEY).equals(VERSION)) {
                // 解析requestBody
                RequestBody requestBody = RpcBuilder.getRequestBody(rpcRequest);

                // 获取请求体中想要调用的方法
                assert requestBody != null;
                String serviceName = requestBody.getInterfaceName();
                Class<?> serviceClass = Class.forName(serviceName);
                Method method = serviceClass
                        .getDeclaredMethod(requestBody.getMethodName(), requestBody.getParamTypes());
                Object serviceInstance = serviceClass.newInstance();
                // 调用函数
                Object res = method.invoke(serviceInstance, requestBody.getParams());

                ResponseBody responseBody = ResponseBody.builder().result(res).build();
                // 写ResponseBody
                RpcResponse rpcResponse = RpcBuilder.buildRpcResponse(RpcBuilder.buildDefaultHeaders(), responseBody);

                // 返回客户端
                oos.writeObject(rpcResponse);
                oos.flush();
            }

        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
