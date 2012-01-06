package samples.echo;

/**
 * Simple Echo server to be used with the Autobahn test suite.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        EchoWsServer server = new EchoWsServer(9001);
        System.out.println("Echo server running on: " + server.start());
    }

}
