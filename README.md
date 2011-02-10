Webbit - A Java event based WebSocket and HTTP server
=====================================================

Getting it
-----------

Warning: Webbit has not had a stable release yet - the API changes regularly.

    git clone git://github.com/joewalnes/webbit.git
    cd webbit
    make

You will need to add build/webbit.jar and lib/netty-3.2.3.Final.jar (its only dependency) to your classpath.

Quick start
-----------

Start a web server on port 8080 and serve some static files:

    WebServer webServer = WebServers.createWebServer(8080)
      .add(new StaticFileHandler("/web")) // path to web content
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
          .add(new StaticFileHandler("/web"))
          .start();
        System.out.println("Server running at " + webServer.getUri());
      }
    }
    
And a page that uses the WebSocket (web/index.html)

    <html>
      <body>

        <!-- Send text to websocket -->
        <input id="userInput" type="text">
        <button onclick="ws.send(document.getElementById('userInput').value">Send</button>

        <!-- Results -->
        <div id="message"></div>

        <script>
          function showMessage(text) {
            document.getElementById('message').innerHTML = text;
          }

          var ws = new WebSocket('ws://' + document.location.host + '/hellowebsocket');
          showMessage('Connecting...');
          ws.onopen = function() { showMessage('Connected!'); };
          ws.onclose = function() { showMessage('Lost connection'); };
          ws.onmessage = function(msg) { showMessage(msg.data); };
        </script>
      </body>
    </html>


More
-----------

+   A [web based chat room](https://github.com/joewalnes/webbit/tree/master/src/sample/java/chatroom) is available in the samples directory.
+   Jay Fields has written a [WebSockets with Clojure introduction](http://blog.jayfields.com/2011/02/clojure-web-socket-introduction.html) that uses Webbit
