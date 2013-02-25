package samples.hello;

import org.webbitserver.*;

public class Hello implements HttpListener{

    public void onOpen(Integer id, HttpRequest request) {
        System.out.println("onOpen:"+ Integer.toString(id));
    }
    public void onClose(Integer id) {
        System.out.println("onclose:"+ Integer.toString(id));
    }
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        System.out.println("handleHttpRequest");
        response.content("Hello World").end();
    }
}