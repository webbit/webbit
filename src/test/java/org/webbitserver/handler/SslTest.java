package org.webbitserver.handler;

import java.io.IOException;
import java.io.FileInputStream;
import javax.net.ssl.HttpsURLConnection;

import org.junit.Test;
import org.junit.BeforeClass;
import org.webbitserver.WebServer;
import org.webbitserver.handler.ServerHeaderHandler;
import org.webbitserver.handler.StringHttpHandler;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpsGet;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

public class SslTest {

    @Test
    public void setsSecureHttpsServerHeader() throws Exception {
        FileInputStream fis = new FileInputStream("src/test/resources/ssl/keystore");
        WebServer webServer = createWebServer(10443)
                .setupSsl(fis, "webbit")
                .add(new ServerHeaderHandler("My Server"))
                .add(new StringHttpHandler("text/plain", "body"));

        fis.close();
        webServer.start();

        try {
            HttpsURLConnection  urlConnection = httpsGet(webServer, "/");
            assertEquals("My Server", urlConnection.getHeaderField("Server"));
            assertEquals("body", contents(urlConnection));
        } finally {
            webServer.stop().join();
        }
    }

    @BeforeClass
    public static void disableCertValidationSetUp() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }
}