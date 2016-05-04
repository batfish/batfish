package org.batfish.common;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.ClientBuilder;

public final class Util {

   private static class TrustAllHostNameVerifier implements HostnameVerifier {
      @Override
      public boolean verify(String hostname, SSLSession session) {
         return true;
      }
   }

   public static ClientBuilder getClientBuilder(boolean secure, boolean trustAll)
         throws Exception {
      if (secure) {
         if (trustAll) {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { new X509TrustManager() {
               @Override
               public void checkClientTrusted(X509Certificate[] arg0,
                     String arg1) throws CertificateException {
               }

               @Override
               public void checkServerTrusted(X509Certificate[] arg0,
                     String arg1) throws CertificateException {
               }

               @Override
               public X509Certificate[] getAcceptedIssuers() {
                  return new X509Certificate[0];
               }

            } }, new java.security.SecureRandom());

            return ClientBuilder.newBuilder().sslContext(sslcontext)
                  .hostnameVerifier(new TrustAllHostNameVerifier());
         }
         else {
            return ClientBuilder.newBuilder();
         }
      }
      else {
         return ClientBuilder.newBuilder();
      }
   }

   public static File getConfigProperties(Class<?> locatorClass,
         String propertiesFilename) {
      File configDir = getJarOrClassDir(locatorClass);
      return Paths.get(configDir.toString(), propertiesFilename).toFile();
   }

   public static File getJarOrClassDir(Class<?> locatorClass) {
      File locatorDirFile = null;
      URL locatorSourceURL = locatorClass.getProtectionDomain().getCodeSource()
            .getLocation();
      String locatorSourceString = locatorSourceURL.toString();
      if (locatorSourceString.startsWith("onejar:")) {
         URL onejarSourceURL = null;
         try {
            onejarSourceURL = Class.forName("com.simontuffs.onejar.Boot")
                  .getProtectionDomain().getCodeSource().getLocation();
         }
         catch (ClassNotFoundException e) {
            throw new BatfishException("could not find onejar class");
         }
         File jarDir = new File(onejarSourceURL.toString().replaceAll(
               "^file:\\\\*", "")).getParentFile();
         return jarDir;
      }
      else {
         char separator = System.getProperty("file.separator").charAt(0);
         String locatorPackageResourceName = locatorClass.getPackage()
               .getName().replace('.', separator);
         try {
            locatorDirFile = new File(locatorClass.getClassLoader()
                  .getResource(locatorPackageResourceName).toURI());
         }
         catch (URISyntaxException e) {
            throw new BatfishException("Failed to resolve locator directory", e);
         }
         assert Boolean.TRUE;
         return locatorDirFile;
      }
   }

   public static String joinStrings(String delimiter, String[] parts) {
      StringBuilder sb = new StringBuilder();
      for (String part : parts) {
         sb.append(part + delimiter);
      }
      String joined = sb.toString();
      int joinedLength = joined.length();
      String result;
      if (joinedLength > 0) {
         result = joined.substring(0, joinedLength - delimiter.length());
      }
      else {
         result = joined;
      }
      return result;
   }

   private Util() {
   }

}
