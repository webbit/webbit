package org.webbitserver.netty;

import org.junit.Test;
import org.webbitserver.WebServer;
import org.webbitserver.handler.StringHttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;

public class FlashPolicyFileTest {

	private static final String EXPECTED = "<?xml version=\"1.0\"?>\r\n"
			+ "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">\r\n"
			+ "<cross-domain-policy>\r\n"
			+ "  <site-control permitted-cross-domain-policies=\"master-only\"/>\r\n"
			+ "  <allow-access-from domain=\"*\" to-ports=\"59504\" />\r\n"
			+ "</cross-domain-policy>\r\n";

	@Test
	public void returnsCrossDomainXML() throws IOException,
			InterruptedException {
		WebServer webServer = createWebServer(59504).add(
				new StringHttpHandler("text/plain", "body")).start();
		try {

			Socket client = new Socket(InetAddress.getLocalHost(), 59504);
			OutputStream out = client.getOutputStream();
			out.write(("<policy-file-request/>\0").getBytes("ASCII"));
			out.flush();
			InputStream in = client.getInputStream();
			String result = convertStreamToString(in);
			client.close();

			assertEquals(EXPECTED, result);

		} finally {
			webServer.stop().join();
		}
	}

	private String convertStreamToString(InputStream is) {
		return new Scanner(is).useDelimiter("\\A").next();
	}

}
