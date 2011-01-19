package chatroom;

import webbit.WebServer;
import webbit.handler.StaticDirectoryHttpHandler;
import webbit.netty.NettyWebServer;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static webbit.route.Route.route;
import static webbit.route.Route.socket;

public class Main {

    public static void main(String[] args) throws Exception {
        Executor executor = Executors.newSingleThreadExecutor();

        WebServer webServer = new NettyWebServer(executor, 9876, route(
                new StaticDirectoryHttpHandler(new File("./src/sample/java/chatroom/content"), executor),
                socket("/chatsocket", new Chatroom())));

        webServer.start();
        System.out.println("Chat room running on: " + webServer.getUri());
    }

}
