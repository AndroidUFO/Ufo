package com.androidufo.ufo.okhttp.ssl;

import com.androidufo.commons.utils.EmptyUtils;
import com.androidufo.ufo.utils.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SSLCreator {

    public static SSLConfigs createSSLFactory(InputStream bksFile, String password, InputStream... certificates) {
        SSLConfigs sslConfigs = new SSLConfigs();
        try {
            KeyManager[] keyManagers = parseToKeyManager(bksFile, password);
            TrustManager[] trustManagers = parseCertificates(certificates);
            X509TrustManager x509TrustManager;
            if (trustManagers == null) {
                x509TrustManager = new UnSafeTrustManager();
            } else {
                x509TrustManager = new SafeTrustManager(getX509TrustManager(trustManagers));
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, new TrustManager[]{x509TrustManager}, null);
            sslConfigs.sslSocketFactory = sslContext.getSocketFactory();
            sslConfigs.trustManager = x509TrustManager;
            sslConfigs.hostnameVerifier = new TrustAllHostnameVerifier();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslConfigs;
    }

    private static KeyManager[] parseToKeyManager(InputStream bksFile, String password) {
        if (bksFile == null || EmptyUtils.stringNull(password)) {
            return null;
        }
        try {
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bksFile, password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static TrustManager[] parseCertificates(InputStream... certificates) {
        int len = certificates == null ? 0 : certificates.length;
        if (certificates == null || certificates.length == 0) {
            return null;
        }
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                if (certificate == null) continue;
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    certificate.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static X509TrustManager getX509TrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    private static class SafeTrustManager implements X509TrustManager {

        private final X509TrustManager defaultTrustManager;
        private final X509TrustManager certificateTrustManager;

        public SafeTrustManager(X509TrustManager certificateTrustManager) throws Exception {
            this.certificateTrustManager = certificateTrustManager;
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            this.defaultTrustManager = getX509TrustManager(factory.getTrustManagers());
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException ce) {
                certificateTrustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class UnSafeTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static class SSLConfigs {
        public HostnameVerifier hostnameVerifier;
        public SSLSocketFactory sslSocketFactory;
        public X509TrustManager trustManager;
    }

}

