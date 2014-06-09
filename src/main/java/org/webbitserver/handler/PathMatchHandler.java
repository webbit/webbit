package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathMatchHandler implements HttpHandler {

    private final Pattern pathPattern;
    private final HttpHandler httpHandler;

    public PathMatchHandler(Pattern pathPattern, HttpHandler httpHandler) {
        this.pathPattern = pathPattern;
        this.httpHandler = httpHandler;
    }

    public PathMatchHandler(String path, HttpHandler httpHandler) {
        this(Pattern.compile(path), httpHandler);
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
			Matcher matcher;

			try
			{
				String path = URI.create(request.uri()).getPath();
				matcher = pathPattern.matcher(path);
			}
			catch(Exception e)
			{
				response.status(400);
				response.header("Content-Type", "text/plain");
				response.content("Internal server error. We are notified about the problem and will fix it. Sorry for any inconvenience.");
				response.end();

				return;
			}

				if(matcher.matches())
				{
					httpHandler.handleHttpRequest(request, response, control);
				}
				else
				{
					control.nextHandler();
				}
    }
}
