package server;

import client.RpcProxy;
import core.RpcRegister;
import hello.HelloRequest;
import hello.HelloResponse;
import hello.HelloService;
import hello.HelloServiceImpl;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.Executors;

public class RpcTestServer {
    @Test
    public void test(){
        RpcRegister rpcRegister = new RpcRegister();
        rpcRegister.start(9000);
    }


    @Test
    public void testServer(){
        RpcServer rpcServer = new RpcServer(Executors.newSingleThreadExecutor());
        try {
            rpcServer.registerService("localhost", 9000, new HelloServiceImpl());
            rpcServer.start(9001);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Test
    public void testClient(){
        HelloService proxy = new RpcProxy().getProxy(HelloService.class);
        HelloResponse response = proxy.hello(new HelloRequest("hoshino"));
        System.out.println(response.getMsg());
    }

    @Test
    public void test1(){
        String s = "a";
        System.out.println(s.substring(1));
    }

}
