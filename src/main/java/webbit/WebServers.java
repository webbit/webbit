package webbit;

import webbit.netty.NettyWebServer;

import java.util.concurrent.Executor;

public class WebServers {

    public static WebServer createWebServer(int port) {
        return new NettyWebServer(port);
    }

    public static WebServer createWebServer(Executor executor, int port) {
        return new NettyWebServer(executor, port);
    }

}
