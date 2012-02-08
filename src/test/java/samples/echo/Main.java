package samples.echo;

import org.webbitserver.netty.NettyWebServer;

/**
 * Simple Echo server to be used with the Autobahn test suite.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        EchoWsServer server = new EchoWsServer(new NettyWebServer(9001));
        server.start();
        System.out.println("Echo server running on: " + server.uri());
    }

}
