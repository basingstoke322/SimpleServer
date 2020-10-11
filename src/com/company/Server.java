package com.company;

import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class Server {
    public static void main(String[] args) {
        try {
            // Set up the socket address
            InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 8090);

            // Initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            //Initialise the keystore
            char[] password = "passw0rd".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("keystore.jks");
            ks.load(fis, password);

            // Set up the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // Set up the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // Set up the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext)
            {
                public void configure(HttpsParameters params)
                {
                    try {
                        // Initialise the SSL context
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    }
                    catch (Exception ex)
                    {
                        System.out.println(ex.getMessage());
                    }
                }
            });
            httpsServer.createContext("/", httpExchange -> {
                System.out.println("get request");

                OutputStream os = httpExchange.getResponseBody();
                byte[] s = "<html></html>".getBytes();
                httpExchange.sendResponseHeaders(200, s.length);
                os.write(s);
                os.flush();
            });
            httpsServer.start();
            System.out.println("Service started");
        }
        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
        }
    }
}
