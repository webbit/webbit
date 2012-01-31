package org.webbitserver.helpers;

import org.webbitserver.WebbitException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

public class SslFactory {
    private static final String PROTOCOL = "TLS";
    private final KeyStore ks;

    public SslFactory(InputStream keyStore, String storePass) {
        try {
            // Create and load keystore file
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(keyStore, storePass.toCharArray());
        } catch (Exception e) {
            throw new WebbitException(e);
        }
    }

    public SSLContext getServerContext(String keyPass) throws WebbitException {
        try {
            // Set up key manager factory to use our key store
            String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
            if (algorithm == null) algorithm = "SunX509";
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, keyPass.toCharArray());

            // Initialize the SSLContext to work with our key managers.
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(kmf.getKeyManagers(), null, null);
            return sslContext;
        } catch (Exception e) {
            throw new WebbitException(e);
        }
    }

    public SSLContext getClientContext() throws WebbitException {
        try {
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
            tmf.init(ks);
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sslContext.init(null, trustManagers, null);
            return sslContext;
        } catch (Exception e) {
            throw new WebbitException(e);
        }
    }
}
