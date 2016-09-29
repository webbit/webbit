package org.webbitserver.rest;

import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.handler.HttpVerbHandler;
import org.weborganic.furi.Parameters;
import org.weborganic.furi.ResolvedVariables;
import org.weborganic.furi.URIParameters;
import org.weborganic.furi.URITemplate;

/**
 * Sinatra-style API around Webbit. Useful for defining RESTful APIs. Paths are defined according to the
 * <a href="http://tools.ietf.org/html/draft-gregorio-uritemplate-07">uritemplate</a> specification.
 */
public class Rest {
    private WebServer webServer;

    public Rest(WebServer webServer) {
        this.webServer = webServer;
    }

    public Rest GET(String uriTemplate, HttpHandler httpHandler) {
        return verbHandler("GET", uriTemplate, httpHandler);
    }

    public Rest PUT(String uriTemplate, HttpHandler httpHandler) {
        return verbHandler("PUT", uriTemplate, httpHandler);
    }

    public Rest POST(String uriTemplate, HttpHandler httpHandler) {
        return verbHandler("POST", uriTemplate, httpHandler);
    }

    public Rest DELETE(String uriTemplate, HttpHandler httpHandler) {
        return verbHandler("DELETE", uriTemplate, httpHandler);
    }

    public Rest HEAD(String uriTemplate, HttpHandler httpHandler) {
        return verbHandler("HEAD", uriTemplate, httpHandler);
    }

    private Rest verbHandler(String verb, String uriTemplate, HttpHandler httpHandler) {
        webServer.add(new UriTemplateHandler(uriTemplate, new HttpVerbHandler(verb, httpHandler)));
        return this;
    }

    /**
     * Get the resolved URI-template variables associated with {@code request}.
     *
     * @param request the request holding the params
     * @param name    named segment from the uri-template
     * @return the parameter value
     */
    public static Object param(HttpRequest request, String name) {
        return params(request).get(name);
    }

    /**
     * Get the resolved URI-template variables associated with {@code request}.
     *
     * @param request the request holding the params
     * @return an object with all resolved variables
     */
    public static ResolvedVariables params(HttpRequest request) {
        return (ResolvedVariables) request.data(UriTemplateHandler.RESOLVED_VARIABLES);
    }

    /**
     * Perform a 302 Redirect
     *
     * @param response      the response to redirect
     * @param uriTemplate   where to redirect
     * @param keyValuePairs Example: ["name", "Mickey", "pet", "Pluto"]
     */
    public static void redirect(HttpResponse response, String uriTemplate, String... keyValuePairs) {
        redirect(response, uriTemplate, params(keyValuePairs));
    }

    /**
     * Perform a 302 Redirect
     *
     * @param response    the response to redirect
     * @param uriTemplate where to redirect
     * @param parameters  filled into the {@code uriTemplate}
     */
    public static void redirect(HttpResponse response, String uriTemplate, Parameters parameters) {
        String uri = URITemplate.expand(uriTemplate, parameters);
        response.header("Location", uri).status(302).end();
    }

    private static Parameters params(String[] parameters) {
        Parameters params = new URIParameters();
        for (int i = 0; i < parameters.length; i += 2) {
            params.set(parameters[i], parameters[i + 1]);
        }
        return params;
    }

}
