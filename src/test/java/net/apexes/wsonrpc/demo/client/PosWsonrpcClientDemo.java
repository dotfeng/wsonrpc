package net.apexes.wsonrpc.demo.client;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.apexes.jsonrpc.GsonJsonContext;
import net.apexes.jsonrpc.JsonRpcLogger;
import net.apexes.wsonrpc.WsonrpcConfig;
import net.apexes.wsonrpc.WsonrpcRemote;
import net.apexes.wsonrpc.client.WsonrpcClientListener;
import net.apexes.wsonrpc.client.WsonrpcClient;
import net.apexes.wsonrpc.demo.api.RegisterService;
import net.apexes.wsonrpc.demo.api.User;

/**
 * 
 * @author <a href="mailto:hedyn@foxmail.com">HeDYn</a>
 *
 */
public class PosWsonrpcClientDemo {
    
    private static ExecutorService execService = Executors.newCachedThreadPool();
    
    public static void main(String[] args) throws Exception {
        GsonJsonContext jsonContext = new GsonJsonContext();
        jsonContext.setLogger(new JsonRpcLogger() {

            @Override
            public void onRead(String json) {
                System.err.println("onRead: " + json);
            }

            @Override
            public void onWrite(String json) {
                System.err.println("onWrite: " + json);
            }
        });
        WsonrpcConfig config = WsonrpcConfig.Builder.create().jsonContext(jsonContext).build(execService);
        URI uri = new URI("ws://127.0.0.1:8080");
        WsonrpcClient client = WsonrpcClient.Builder.create(uri).build(config);
        client.setClientListener(new WsonrpcClientListener() {

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }

            @Override
            public void onOpen(WsonrpcClient client) {
            }

            @Override
            public void onClose(WsonrpcClient client, int code, String reason) {
            }

            @Override
            public void onSentMessage(byte[] bytes) {
            }

            @Override
            public void onSentPing() {
            }
        });
        
        // 供Server端调用的接口
        client.getServiceRegistry().register(new CallPosServiceImpl());
        
        client.connect();
        
        String posId = UUID.randomUUID().toString().replace("-", "");
        System.out.println("POS_ID="+posId);
        
        // 同步调用
        RegisterService registerService = WsonrpcRemote.Executor.create(client)
                .getService(RegisterService.class);
        registerService.registerPos(posId);
        
        // 异步调用
        User asyncUser = new User();
        asyncUser.setUsername("async");
        asyncUser.setPassword("abcdef");
        Future<User> future = client.asyncInvoke(RegisterService.class.getName(), "registerUser",
                asyncUser, User.class);
        System.out.println("async: " + future.get(10, TimeUnit.SECONDS));
        
        Thread.sleep(1000);
        
        client.disconnect();
        execService.shutdownNow();
        System.out.println("Over!");
    }

}