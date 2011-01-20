package chatroom;

import webbit.WebServer;
import webbit.netty.NettyWebServer;

public class Main {

    public static void main(String[] args) throws Exception {
        WebServer webServer = new NettyWebServer(9876)
                .add("/chatsocket", new Chatroom())
                .staticResources("./src/sample/java/chatroom/content")
                .start();

        System.out.println("Chat room running on: " + webServer.getUri());
    }

}
