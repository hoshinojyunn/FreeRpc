package client;


import core.RpcRequest;
import core.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 传输层
 */
public class RpcTransfer {

    public RpcResponse sendRequest(RpcRequest request, String host, int port){
        // 发送到服务注册中心
        try (Socket socket = new Socket(host, port)){
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(request);
            oos.flush();
            // 监听返回结果
            return (RpcResponse) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



}
