# 一、介绍
FreeRPC是一个简易的RPC框架，包括客户端、服务端、服务注册中心三方，设计上参考dubbo。
支持任意接口的服务注册，服务的注册与调用分为以下两个步骤：
* 服务端实现接口方法，而后将实现类与服务端的地址与端口传递给注册中心完成服务注册。
* 客户端要求与服务端定义一致的接口，这里的一致包括接口名、接口方法、与接口所在包的一致，
而后客户端利用RpcProxy代理类动态代理接口，代理类调用接口方法即可实现远程调用服务端的服务实现。
# 二、简单示例
在hello包下有接口HelloService以及实现类HelloServiceImpl，
我们需要利用服务端将实现注册到注册中心，而后用客户端代理接口调用实现类
1. 创建注册中心
```java
@Test
public void test(){
    RpcRegister rpcRegister = new RpcRegister();
    // 注册中心启动，参数为端口
    rpcRegister.start(9000);
}
```
2. 服务端注册服务
```java
@Test
public void testServer(){
    RpcServer rpcServer = new RpcServer(Executors.newSingleThreadExecutor());
    try {
        // 填写注册中心的ip与端口，完成服务注册
        rpcServer.registerService("localhost", 9000, new HelloServiceImpl());
        // 服务端启动，参数为端口
        rpcServer.start(9001);
    } catch (IOException e) {
        e.printStackTrace();
    }

}
```
3. 客户端调用服务
```java
@Test
public void testClient(){
    // 代理接口
    HelloService proxy = new RpcProxy().getProxy(HelloService.class);
    // 调用服务
    HelloResponse response = proxy.hello(new HelloRequest("hoshino"));
    System.out.println(response.getMsg());
}
```
4. 结果
```text
hello hoshino
```
