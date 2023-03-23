package core;

import codec.RequestBody;
import codec.ResponseBody;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

import static core.RpcConstants.*;

public class RpcRegister {

    // 接口名称与其对应的实现类的名称
    private Map<String, String> registerService;
    private Map<String, String[]>serviceRemoteAddress;
    private ThreadPoolExecutor threadPoolExecutor;

    public RpcRegister() {
        registerService = new ConcurrentHashMap<>();
        serviceRemoteAddress = new ConcurrentHashMap<>();
        threadPoolExecutor = new ThreadPoolExecutor(5,
                10,
                2000,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1024 * 1024),
                Executors.defaultThreadFactory());
    }

    public Map<String, String> getRegisterService() {
        return registerService;
    }

    public void setRegisterService(Map<String, String> registerService) {
        this.registerService = registerService;
    }


    private class ServeQueryHandler implements Runnable{
        private ObjectOutputStream handlerSocketOutputStream;
        private RpcRequest rpcRequest;

        public ServeQueryHandler(ObjectOutputStream handlerSocketOutputStream, RpcRequest rpcRequest){
            this.handlerSocketOutputStream = handlerSocketOutputStream;
            this.rpcRequest = rpcRequest;
        }

        public void serveClient(){
            try{
                // 检查版本
                if(VERSION.equals(rpcRequest.getHeaders().get(VERSION_KEY))){
                    RequestBody requestBody = RpcBuilder.getRequestBody(rpcRequest);
                    String interfaceName = requestBody.getInterfaceName();
                    System.out.println("客户端查询:" + interfaceName + "实现");
                    // 获得实现类名
                    String implementServiceName = registerService.get(interfaceName);
                    // 改变请求体 将接口名改成实现类名
                    requestBody.setInterfaceName(implementServiceName);
                    // 修改rpc请求
                    rpcRequest.setBody(RpcBuilder.toByteArray(requestBody));
                    Map<String, String>headers = new HashMap<>();
                    // 设置远程服务的地址信息
                    headers.put(VERSION_KEY, "1");
                    System.out.println(serviceRemoteAddress);
                    headers.put(REMOTE_HOST, serviceRemoteAddress.get(interfaceName)[0]);
                    headers.put(REMOTE_PORT, serviceRemoteAddress.get(interfaceName)[1]);
                    rpcRequest.setHeaders(headers);

                    // 回传客户端
                    handlerSocketOutputStream.writeObject(new RpcResponse(RpcBuilder.buildDefaultHeaders(),
                            RpcBuilder.toByteArray(new ResponseBody(rpcRequest))));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            serveClient();
        }
    }
    /**
     * 监听服务器传来的注册请求
     * @param
     */
    private class ServeRegisterHandler implements Runnable{
        private ObjectOutputStream handlerSocketOutputStream;
        private RpcRequest rpcRequest;
        public ServeRegisterHandler(ObjectOutputStream handlerSocketOutputStream, RpcRequest rpcRequest){
            this.handlerSocketOutputStream = handlerSocketOutputStream;
            this.rpcRequest = rpcRequest;
        }

        public void serveRegister(){
            try {
                // 服务注册需要传递一个List 分别放接口名 实现类名 远程服务器host 远程服务器port
                String interfaceName = null, implServiceName = null, remoteHost = null, remotePort = null;
                Map<String, String> headers = rpcRequest.getHeaders();

                interfaceName = headers.get(INTERFACE_NAME);
                implServiceName = headers.get(IMPLEMENT_NAME);
                remoteHost = headers.get(REMOTE_HOST);
                remotePort = headers.get(REMOTE_PORT);
                // 注册
                registerService.put(interfaceName,
                        implServiceName);
                serviceRemoteAddress.put(interfaceName, new String[]{remoteHost, remotePort});
                handlerSocketOutputStream.writeObject(REGISTER_OK);
            }catch (IOException e) {
                e.printStackTrace();
                try {
                    assert handlerSocketOutputStream != null;
                    handlerSocketOutputStream.writeObject(REGISTER_FAIL);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            serveRegister();
        }
    }

    // 注册端口 9000
    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket handlerSocket = null;
            while((handlerSocket = serverSocket.accept()) != null){
                // 解析请求
                ObjectInputStream ois = new ObjectInputStream(handlerSocket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(handlerSocket.getOutputStream());
                RpcRequest req = (RpcRequest)ois.readObject();
                String type = req.getHeaders().get(TYPE_KEY);
                if(type.equals(QUERY_TYPE))
                    threadPoolExecutor.execute(new ServeQueryHandler(oos, req));
                else if(type.equals(REGISTER_TYPE))
                    threadPoolExecutor.execute(new ServeRegisterHandler(oos, req));
            }
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}
