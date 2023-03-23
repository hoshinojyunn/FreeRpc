package hello;

import hello.HelloRequest;
import hello.HelloResponse;
import hello.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public HelloResponse hello(HelloRequest request) {
        HelloResponse response = new HelloResponse();
        response.setMsg("hello " + request.getName());
        return response;
    }
}
