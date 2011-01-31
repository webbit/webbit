package chatroom;

import webbit.WebServer;
import webbit.handler.LoggingHandler;

import static webbit.WebServers.createWebServer;

public class Main {

    public static void main(String[] args) throws Exception {
        WebServer webServer = createWebServer(9876)
                .add(new LoggingHandler())
                .add("/chatsocket", new Chatroom())
                .staticResources("./src/sample/java/chatroom/content")
                .start();

        System.out.println("Chat room running on: " + webServer.getUri());
    }

}
