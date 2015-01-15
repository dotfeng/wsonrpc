package net.apexes.wsonrpc.demo.server;

import java.util.Arrays;
import java.util.concurrent.Executors;

import javax.websocket.server.ServerEndpoint;

import net.apexes.wsonrpc.ExceptionProcessor;
import net.apexes.wsonrpc.WsonrpcConfig;
import net.apexes.wsonrpc.demo.api.LoginService;
import net.apexes.wsonrpc.service.WsonrpcServiceEndpoint;

import org.glassfish.tyrus.core.MaxSessions;

/**
 * 
 * @author <a href=mailto:hedyn@foxmail.com>HeDYn</a>
 *
 */
@MaxSessions(10000)
@ServerEndpoint("/wsonrpc")
public class WsonrpcService extends WsonrpcServiceEndpoint implements ExceptionProcessor {

    public WsonrpcService() {
        super(WsonrpcConfig.Builder.create().build(Executors.newCachedThreadPool()));
        this.setExceptionProcessor(this);
        this.addService(LoginService.class.getSimpleName(), new LoginServiceImpl());
    }

    @Override
    public void onError(Throwable error, Object... params) {
        if (params != null) {
            System.err.println(Arrays.toString(params));
        }
        error.printStackTrace();
    }
}
