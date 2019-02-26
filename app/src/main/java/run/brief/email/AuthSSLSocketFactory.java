package run.brief.email;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class AuthSSLSocketFactory  extends SSLSocketFactory {

    private SSLSocketFactory factory;
        
    public static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) 
        throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
        throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public AuthSSLSocketFactory() 
    throws NoSuchAlgorithmException, KeyManagementException {
        super();
//BLog.e("SOCKET","-------------------AuthSSLSocketFactory");
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {
            new DefaultTrustManager()}, new SecureRandom());
            factory = (SSLSocketFactory) ctx.getSocketFactory();
    }

    public static SocketFactory getDefault() {
        try {
            return new AuthSSLSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate default Brief AuthSSLSocketFactory", e);
        }
    }

    public Socket createSocket() throws IOException {
        return factory.createSocket();
    }

    public Socket createSocket(InetAddress address, int port, 
    InetAddress localAddress, int localPort)
    throws IOException {
        return factory.createSocket(address, port, localAddress, localPort);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return factory.createSocket(host, port);
    }

    public Socket createSocket(Socket s, String host, int port, 
    boolean autoClose) throws IOException {
        return factory.createSocket(s, host, port, autoClose);
    }

    public Socket createSocket(String host, int port, InetAddress localHost, 
    int localPort) throws IOException, UnknownHostException {
        return factory.createSocket(host, port, localHost, localPort);
    }

    public Socket createSocket(String host, int port) throws IOException, 
    UnknownHostException {
        return factory.createSocket(host, port);
    }

    public boolean equals(Object obj) {
        return factory.equals(obj);
    }

    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

}