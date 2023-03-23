package core;

import codec.RequestBody;
import codec.ResponseBody;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static core.RpcConstants.*;

public class RpcBuilder {
    private static class RpcRequestBuilder{
        public static RpcRequest build(Map<String, String>headers, RequestBody requestBody){
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(requestBody);
                return new RpcRequest(headers, bos.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class RpcResponseBuilder{
        public static RpcResponse build(Map<String, String>headers, ResponseBody responseBody){
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(responseBody);
                return new RpcResponse(headers, bos.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static RpcRequest buildRpcRequest(Map<String, String>headers, RequestBody requestBody){
        return RpcRequestBuilder.build(headers, requestBody);
    }

    public static RpcResponse buildRpcResponse(Map<String, String>headers, ResponseBody responseBody){
        return RpcResponseBuilder.build(headers, responseBody);
    }

    public static RequestBody getRequestBody(RpcRequest rpcRequest){
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(rpcRequest.getBody());
            ObjectInputStream requestBodyInputStream = new ObjectInputStream(bis);
            return (RequestBody) requestBodyInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResponseBody getResponseBody(RpcResponse rpcResponse){
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(rpcResponse.getBody());
            ObjectInputStream requestBodyInputStream = new ObjectInputStream(bis);
            return (ResponseBody) requestBodyInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static byte[] toByteArray(Object object){
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream requestBodyOutputStream = new ObjectOutputStream(bos);
            requestBodyOutputStream.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String,String> buildDefaultHeaders(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put(VERSION_KEY, VERSION);
        return headers;
    }

    public static Map<String, String> buildQueryHeaders(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put(VERSION_KEY, VERSION);
        headers.put(TYPE_KEY, QUERY_TYPE);
        return headers;
    }

    public static Map<String, String> buildRegisterHeaders(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put(VERSION_KEY, VERSION);
        headers.put(TYPE_KEY, REGISTER_TYPE);
        return headers;
    }

}
