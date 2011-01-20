package webbit.handler;

import webbit.HttpControl;
import webbit.HttpHandler;
import webbit.HttpRequest;
import webbit.HttpResponse;

public class NotFoundHttpHandler implements HttpHandler {

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
        response.status(404).end();
    }

}
