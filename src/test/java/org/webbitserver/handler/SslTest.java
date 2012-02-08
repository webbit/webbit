package org.webbitserver.handler;

import org.junit.BeforeClass;
import org.junit.Test;
import org.webbitserver.WebServer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpsGet;

public class SslTest {

    @Test
    public void setsSecureHttpsServerHeader() throws Exception {
        InputStream keystore = getClass().getResourceAsStream("/ssl/keystore");
        WebServer webServer = createWebServer(10443)
                .setupSsl(keystore, "webbit")
                .add(new ServerHeaderHandler("My Server"))
                .add(new StringHttpHandler("text/plain", "body"));

        keystore.close();
        webServer.start();

        try {
            HttpsURLConnection urlConnection = httpsGet(webServer, "/");
            assertEquals("My Server", urlConnection.getHeaderField("Server"));
            assertEquals("body", contents(urlConnection));
        } finally {
            webServer.stop().get();
        }
    }

    @BeforeClass
    public static void disableCertValidationSetUp() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
}