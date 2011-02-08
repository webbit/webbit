Webbit - A Java event based WebSocket and HTTP server
=====================================================

Quick start
-----------

Start a web server on port 8080 and serve some static files:

    WebServer webServer = WebServers.createWebServer(8080)
      .add(new StaticFileHandler("/path/to/files"))
      .start();

That was easy.

Now let's build a WebSocketHandler.

    public class HelloWebSockets implements WebSocketHandler {
    
      int connectionCount;
      
      public void onOpen(WebSocketConnection connection) {
        connection.send("Hello! There are " + connectionCount + " other connections active");
        connectionCount++;
      }
      
      public void onClose(WebSocketConnection connection) {
        connectionCount--;
      }
      
      public void onMessage(WebSocketConnection connection, String message) {
        connection.send(message.toUpperCase()); // echo back message in upper case
      }
    
      public static void main(String args) {
        WebServer webServer = WebServers.createWebServer(8080)
          .add("/hellowebsocket", new HelloWebSockets())
          .add(new StaticFileHandler("/path/to/files"))
          .start();
      }
    }
    
Look in the examples directory for a full chat application.