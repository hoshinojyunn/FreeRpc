package server;

import core.RpcBuilder;
import core.RpcRegister;
import core.RpcRequest;
import hello.HelloServiceImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static core.RpcConstants.*;

public class RpcServer {
    private final ExecutorService threadPool;

    public RpcServer(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public void start(int port) throws IOException {

        System.out.println("server start");
        try (ServerSocket serverSocket = new ServerSocket(port)){
            Socket handlerSocket = null;
            while((handlerSocket = serverSocket.accept())!=null) {
                System.out.println("client connect:" + handlerSocket.getInetAddress() + ":" + handlerSocket.getPort());
                threadPool.submit(new RpcServerTask(handlerSocket));
            }
        }

    }

    /**
     * 向注册中心注册服务
     * @param localhost
     * @param port
     * @param serviceImpl
     */
    public void registerService(String localhost, int port, Object serviceImpl) {
        try {
            Socket socket = new Socket(localhost, port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            Map<String, String> headers = RpcBuilder.buildRegisterHeaders();
            headers.put(INTERFACE_NAME, serviceImpl.getClass().getInterfaces()[0].getName());
            headers.put(IMPLEMENT_NAME, serviceImpl.getClass().getName());
            headers.put(REMOTE_HOST, "localhost");
            headers.put(REMOTE_PORT, String.valueOf(9001));
            RpcRequest request = RpcBuilder.buildRpcRequest(headers, null);

            oos.writeObject(request);
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            String res = (String)ois.readObject();
            if(REGISTER_OK.equals(res))
                System.out.println(serviceImpl.getClass().getName() + "注册成功");
            else
                System.out.println(serviceImpl.getClass().getName() + "注册失败");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
