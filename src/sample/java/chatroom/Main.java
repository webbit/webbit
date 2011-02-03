package chatroom;

import webbit.WebServer;
import webbit.handler.logging.LoggingHandler;
import webbit.handler.StaticFileHandler;
import webbit.handler.logging.SimpleLogSink;

import static webbit.WebServers.createWebServer;

public class Main {

    public static void main(String[] args) throws Exception {
        WebServer webServer = createWebServer(9876)
                .add(new LoggingHandler(new SimpleLogSink()))
                .add("/chatsocket", new Chatroom())
                .add(new StaticFileHandler("./src/sample/java/chatroom/content"))
                .start();

        System.out.println("Chat room running on: " + webServer.getUri());
    }

}
