package org.webbitserver.netty;

import org.junit.Test;
import org.webbitserver.WebServer;
import org.webbitserver.handler.StringHttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;

public class FlashPolicyFileTest {

    @Test
    public void returnsCrossDomainXML() throws IOException, InterruptedException, ExecutionException {
        WebServer webServer = createWebServer(59504).add(new StringHttpHandler("text/plain", "body")).start().get();

        try {
            Socket client = new Socket(InetAddress.getLocalHost(), 59504);
            OutputStream out = client.getOutputStream();
            out.write(("<policy-file-request/>\0").getBytes("ASCII"));
            out.flush();
            InputStream in = client.getInputStream();
            String result = convertStreamToString(in);
            client.close();

            assertEquals(getPolicyFile("59504"), result);

        } finally {
            webServer.stop().get();
        }
    }

    @Test
    public void returnsCrossDomainXMLWithPublicPort() throws IOException, InterruptedException, ExecutionException {

        Executor executor = Executors.newSingleThreadScheduledExecutor();
        InetSocketAddress address = new InetSocketAddress(59504);
        URI publicUri = URI.create("http://localhost:800/");

        WebServer webServer = createWebServer(executor, address, publicUri).add(new StringHttpHandler("text/plain", "body")).start().get();
        try {

            Socket client = new Socket(InetAddress.getLocalHost(), 59504);
            OutputStream out = client.getOutputStream();
            out.write(("<policy-file-request/>\0").getBytes("ASCII"));
            out.flush();
            InputStream in = client.getInputStream();
            String result = convertStreamToString(in);
            client.close();

            assertEquals(getPolicyFile("800"), result);

        } finally {
            webServer.stop().get();
        }
    }

    @Test
    public void returnsCrossDomainXMLWithDefaultHTTPPublicPort() throws IOException, InterruptedException, ExecutionException {

        Executor executor = Executors.newSingleThreadScheduledExecutor();
        InetSocketAddress address = new InetSocketAddress(59504);
        URI publicUri = URI.create("http://localhost/");

        WebServer webServer = createWebServer(executor, address, publicUri).add(new StringHttpHandler("text/plain", "body")).start().get();
        try {

            Socket client = new Socket(InetAddress.getLocalHost(), 59504);
            OutputStream out = client.getOutputStream();
            out.write(("<policy-file-request/>\0").getBytes("ASCII"));
            out.flush();
            InputStream in = client.getInputStream();
            String result = convertStreamToString(in);
            client.close();

            assertEquals(getPolicyFile("80"), result);

        } finally {
            webServer.stop().get();
        }
    }

    @Test
    public void returnsCrossDomainXMLWithDefaultHTTPSPublicPort() throws IOException, InterruptedException, ExecutionException {

        Executor executor = Executors.newSingleThreadScheduledExecutor();
        InetSocketAddress address = new InetSocketAddress(59504);
        URI publicUri = URI.create("https://localhost/");

        WebServer webServer = createWebServer(executor, address, publicUri).add(new StringHttpHandler("text/plain", "body")).start().get();
        try {

            Socket client = new Socket(InetAddress.getLocalHost(), 59504);
            OutputStream out = client.getOutputStream();
            out.write(("<policy-file-request/>\0").getBytes("ASCII"));
            out.flush();
            InputStream in = client.getInputStream();
            String result = convertStreamToString(in);
            client.close();

            assertEquals(getPolicyFile("443"), result);

        } finally {
            webServer.stop().get();
        }
    }

    private String getPolicyFile(String port) {
        String policyFile = "<?xml version=\"1.0\"?>\r\n"
                + "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">\r\n"
                + "<cross-domain-policy>\r\n"
                + "  <site-control permitted-cross-domain-policies=\"master-only\"/>\r\n"
                + "  <allow-access-from domain=\"*\" to-ports=\"" + port + "\" />\r\n"
                + "</cross-domain-policy>\r\n";
        return policyFile;
    }

    private String convertStreamToString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }

}
