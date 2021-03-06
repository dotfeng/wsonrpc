package net.apexes.wsonrpc.demo.client;

import net.apexes.wsonrpc.client.Wsonrpc;
import net.apexes.wsonrpc.client.WsonrpcClient;
import net.apexes.wsonrpc.client.WsonrpcClientListener;
import net.apexes.wsonrpc.demo.api.DemoService;
import net.apexes.wsonrpc.demo.api.PushService;
import net.apexes.wsonrpc.demo.api.RegisterService;
import net.apexes.wsonrpc.demo.api.model.User;
import net.apexes.wsonrpc.demo.client.service.PushServiceImpl;
import net.apexes.wsonrpc.demo.util.SimpleWsonrpcErrorProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @author <a href="mailto:hedyn@foxmail.com">HeDYn</a>
 *
 */
public class DemoWsonrpcClient {
    
    private static final Logger LOG = LoggerFactory.getLogger(DemoWsonrpcClient.class);
    
    private final WsonrpcClient client;
    private String clientId;
    
    DemoWsonrpcClient() throws Exception {
        String url = "ws://localhost:8080/wsonrpc";
        client = Wsonrpc.config()
//                .json(new net.apexes.wsonrpc.json.support.JacksonImplementor())
//                .binaryWrapper(new net.apexes.wsonrpc.core.GZIPBinaryWrapper())
//                .connector(new net.apexes.wsonrpc.client.support.JavaWebsocketConnector())
//                .connector(new net.apexes.wsonrpc.client.support.TyrusWebsocketConnector())
                .errorProcessor(new SimpleWsonrpcErrorProcessor())
                .client(url);
        client.getServiceRegistry().register("push", new PushServiceImpl(), PushService.class);
        
        client.setClientListener(new WsonrpcClientListener() {

            @Override
            public void onOpen(WsonrpcClient client) {
                LOG.info("...");
                Wsonrpc.invoker(client)
                        .serviceName("register")
                        .get(RegisterService.class)
                        .register(clientId);
            }

            @Override
            public void onClose(WsonrpcClient client, int code, String reason) {
                LOG.warn("code={}, reason={}", code, reason);
            }

            @Override
            public void onSentMessage(byte[] bytes) {
                LOG.info("length={}", bytes.length);
            }

            @Override
            public void onSentPing() {
                LOG.info("...");
            }
            
        });
    }
    
    public boolean isConnected() {
        return client.isConnected();
    }
    
    public void connect(String clientId) throws Exception {
        if (!client.isConnected()) {
            this.clientId = clientId;
            client.connect();
        }
    }
    
    public void disconnect() throws Exception {
        if (client.isConnected()) {
            client.disconnect();
        }
    }
    
    public void close() throws Exception {
        client.disconnect();
//        if (client.isConnected()) {
//
//        }
    }
    
    public void login(String username, String password) {
        if (client.isConnected()) {
            RegisterService service = Wsonrpc.invoker(client).serviceName("register").get(RegisterService.class);
            User user = service.login(username, password);
            LOG.info("{}", user);
        }
    }
    
    public void ping() throws Exception {
        if (client.isConnected()) {
            client.ping();
        }
    }
    
    public void demo() {
        DemoService service = Wsonrpc.invoker(client).serviceName("demo").get(DemoService.class);
        LOG.info("{}", service.echo("Hello wsonrpc!"));
        LOG.info("{}", service.login("admin", "admin"));
        LOG.info("{}", service.getRoleList());
        LOG.info("{}", service.getDept("1"));
        LOG.info("{}", service.getDeptList());
        LOG.info("{}", service.listUser(Arrays.asList("admin", "1001")));
    }
    
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        DemoWsonrpcClient client = new DemoWsonrpcClient();
        try {
            while (true) {
                System.out.print(">");
                String command = reader.readLine();
                if (command.isEmpty()) {
                    continue;
                }
                if (command.startsWith("connect ")) {
                    String clientId = command.substring("connect ".length());
                    if (clientId != null && clientId.length() > 0) {
                        client.connect(clientId);
                    }
                } else if ("disconnect".equalsIgnoreCase(command)) {
                    client.disconnect();
                } else if ("exit".equalsIgnoreCase(command)) {
                    client.close();
                    break;
                } else if ("login".equalsIgnoreCase(command)) {
                    client.login("admin", "admin123");
                } else if ("ping".equalsIgnoreCase(command)) {
                    client.ping();
                } else if ("demo".equalsIgnoreCase(command)) {
                    client.demo();
                }
            }
        } finally {
            reader.close();
            client.close();
        }
        LOG.info("closed");
    }

}
