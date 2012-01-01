package org.webbitserver.helpers;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class SslFactory {
    private static final String PROTOCOL = "TLS";

    public static SSLContext getContext(String keyFile, String storePass, String keyPass) throws Exception {
        // Create and load keystore file
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream fis = new FileInputStream(keyFile);
        ks.load(fis, storePass.toCharArray());
        fis.close();

        // Set up key manager factory to use our key store
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) algorithm = "SunX509";
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
        kmf.init(ks, keyPass.toCharArray());

        // Initialize the SSLContext to work with our key managers.
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }
}
