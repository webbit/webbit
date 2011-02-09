package chatroom2;

import webbit.WebServer;
import webbit.handler.StaticFileHandler;
import webbit.handler.logging.LoggingHandler;
import webbit.handler.logging.SimpleLogSink;
import webbit.magic.Magic;

import static webbit.WebServers.createWebServer;

public class Main {

    public static void main(String[] args) throws Exception {
        WebServer webServer = createWebServer(9876)
                .add(new LoggingHandler(new SimpleLogSink(ChatServer.USERNAME_KEY)))
                .add("/chatsocket", new Magic<ChatClient>(ChatClient.class, new ChatServer()))
                .add(new StaticFileHandler("./src/sample/java/chatroom2/content"))
                .add(new StaticFileHandler("./src/main/java/webbit/magic"))
                .start();

        System.out.println("Chat room running on: " + webServer.getUri());
    }

}