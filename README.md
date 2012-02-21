# Webbit - A Java event based WebSocket and HTTP server

## Getting it

[Prebuilt JARs are available](http://search.maven.org/#search%7Cga%7C1%7Cwebbit) from the central Maven repository.

Alternatively, you can get the latest code from Git and build it yourself:

    git clone git://github.com/webbit/webbit.git
    cd webbit

### Make

Build is done with `make`. On OS-X and Linux this should work out of the box. On Solaris, use `gmake`. On Windows you will need Cygwin.

    make

### Maven

    mvn install

## Quick start

Start a web server on port 8080 and serve some static files:

```java
WebServer webServer = WebServers.createWebServer(8080)
    .add(new StaticFileHandler("/web")); // path to web content
    .start();
```

That was easy.

Now let's build a WebSocketHandler.

```java
public class HelloWebSockets extends BaseWebSocketHandler {
    private int connectionCount;

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

    public static void main(String[] args) {
        WebServer webServer = WebServers.createWebServer(8080)
                .add("/hellowebsocket", new HelloWebSockets())
                .add(new StaticFileHandler("/web"));
        webServer.start();
        System.out.println("Server running at " + webServer.getUri());
    }
}
```

And a page that uses the WebSocket (web/index.html)

```html
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
```

## Contributing

### Running JUnit tests

    mvn clean test

or

    make clean test

### Running Autobahn tests

[Autobahn](http://www.tavendo.de/autobahn) is a WebSocket server implemented in Python that comes with an extensive
[test suite](http://www.tavendo.de/autobahn/testsuite.html) that can be used to test other WebSocket servers as well.

We're using it to test Webbit.

Installing Autobahn

    git submodule update --init

Running Autobahn tests

In shell A:

    make echo

In shell B:

    make autobahn

Open `reports/servers/index.html` to see the results.

## More

+   [Docs on wiki](https://github.com/webbit/webbit/wiki)
+   [Webbit mailing list](http://groups.google.com/group/webbit)
+   [@webbitserver](http://twitter.com/webbitserver) on Twitter
+   A [web based chat room](https://github.com/webbit/webbit/tree/master/src/test/java/samples/chatroom) is available in the samples directory. To try it out: 'make chatroom'
+   Jay Fields has written a [WebSockets with Clojure introduction](http://blog.jayfields.com/2011/02/clojure-web-socket-introduction.html) that uses Webbit
