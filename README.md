Webbit - A Java event based WebSocket and HTTP server
=====================================================

Getting it
-----------

Warning: Webbit has not had a stable release yet - the API changes regularly.

    git clone git://github.com/joewalnes/webbit.git
    cd webbit
    make

You will need to add dist/webbit.jar to your classpath - it has no external dependencies.

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
    
      public void onMessage(WebSocketConnection connection, byte[] message) {
      }

      public void onPong(WebSocketConnection connection, String message) {
      }

      public static void main(String[] args) {
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
        <button onclick="ws.send(document.getElementById('userInput').value)">Send</button>

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

Running Autobahn tests
----------------------

[Autobahn](http://www.tavendo.de/autobahn) is a WebSocket server implemented in Python that comes with an extensive 
[test suite](http://www.tavendo.de/autobahn/testsuite.html) that can be used to test other WebSocket servers as well.

We're using it to test Webbit.

Installing Autobahn

    git submodule update --init
    python virtualenv.py --no-site-packages . # create local version of python (installing twisted)

Running Autobahn tests

In shell A:

    make echo

In shell B:

    make autobahn

More
-----------

+   [Webbit mailing list](http://groups.google.com/group/webbit)
+   [@webbitserver](http://twitter.com/webbitserver) on Twitter
+   A [web based chat room](https://github.com/joewalnes/webbit/tree/master/src/test/java/samples/chatroom) is available in the samples directory. To try it out: 'make chatroom'
+   Jay Fields has written a [WebSockets with Clojure introduction](http://blog.jayfields.com/2011/02/clojure-web-socket-introduction.html) that uses Webbit
