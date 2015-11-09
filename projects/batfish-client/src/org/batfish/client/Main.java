package org.batfish.client;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Main {

   public static void main(String[] args) {
      Settings _settings = null;
      try {
         _settings = new Settings(args);

         //set up to trust all SSL certs
         if (_settings.getUseSsl() && _settings.getTrustAllSslCerts()) {

            TrustManager[] trustAllCerts = new TrustManager[] {
                  new X509TrustManager(){
                     public java.security.cert.X509Certificate[] getAcceptedIssuers(){return new java.security.cert.X509Certificate[0];}
                     public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                     public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                  }
            };

            SSLContext sc = SSLContext.getInstance("SSL"); 
            sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());         
         }         
      }
      catch (Exception e) {
         System.err
         .println("org.batfish.client: Initialization failed: "
               + e.getMessage());
         System.exit(1);
      }

      new Client(_settings);
   }
}