package org.batfish.common.util;

import com.google.common.hash.Hashing;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.ClientBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.skyscreamer.jsonassert.JSONAssert;

public class CommonUtil {

  private static class TrustAllHostNameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  }

  public static final String FACT_BLOCK_FOOTER =
      "\n//FACTS END HERE\n" + "   }) // clauses\n" + "} <-- .\n";

  private static String SALT;

  private static final int STREAMED_FILE_BUFFER_SIZE = 1024;

  public static boolean isNullOrEmpty(@Nullable Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  public static String applyPrefix(String prefix, String msg) {
    String[] lines = msg.split("\n");
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      sb.append(prefix + line + "\n");
    }
    return sb.toString();
  }

  public static <T extends Throwable> boolean causedBy(Throwable e, Class<T> causeClass) {
    Set<Throwable> seenCauses = Collections.newSetFromMap(new IdentityHashMap<>());
    return causedBy(e, causeClass, seenCauses);
  }

  private static <T extends Throwable> boolean causedBy(
      Throwable e, Class<T> causeClass, Set<Throwable> seenCauses) {
    if (seenCauses.contains(e)) {
      return false;
    }
    seenCauses.add(e);
    if (causeClass.isInstance(e)) {
      return true;
    } else {
      Throwable cause = e.getCause();
      if (cause != null) {
        return causedBy(cause, causeClass, seenCauses);
      } else {
        return false;
      }
    }
  }

  public static boolean causedByMessage(Throwable e, String searchTerm) {
    Set<Throwable> seenCauses = Collections.newSetFromMap(new IdentityHashMap<>());
    return causedByMessage(e, searchTerm, seenCauses);
  }

  private static boolean causedByMessage(
      Throwable e, String searchTerm, Set<Throwable> seenCauses) {
    if (seenCauses.contains(e)) {
      return false;
    }
    seenCauses.add(e);
    if (e.getMessage().contains(searchTerm)) {
      return true;
    } else {
      Throwable cause = e.getCause();
      if (cause != null) {
        return causedByMessage(cause, searchTerm, seenCauses);
      } else {
        return false;
      }
    }
  }

  public static boolean checkJsonEqual(Object a, Object b) {
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    try {
      String aString = mapper.writeValueAsString(a);
      String bString = mapper.writeValueAsString(b);
      JSONAssert.assertEquals(aString, bString, false);
      return true;
    } catch (Exception e) {
      throw new BatfishException("JSON equality check failed", e);
    } catch (AssertionError err) {
      return false;
    }
  }

  public static long communityStringToLong(String str) {
    String[] parts = str.split(":");
    long high = Long.parseLong(parts[0]);
    long low = Long.parseLong(parts[1]);
    long result = low + (high << 16);
    return result;
  }

  public static <T extends Comparable<T>> int compareCollection(
      Collection<T> lhs, Collection<T> rhs) {
    Iterator<T> l = lhs.iterator();
    Iterator<T> r = rhs.iterator();
    while (l.hasNext()) {
      if (!r.hasNext()) {
        return 1;
      }
      T lVal = l.next();
      T rVal = r.next();
      int ret = lVal.compareTo(rVal);
      if (ret != 0) {
        return ret;
      }
    }
    if (r.hasNext()) {
      return -1;
    }
    return 0;
  }

  public static void copy(Path srcPath, Path dstPath) {
    if (Files.isDirectory(srcPath)) {
      copyDirectory(srcPath, dstPath);
    } else {
      copyFile(srcPath, dstPath);
    }
  }

  public static void copyDirectory(Path srcPath, Path dstPath) {
    try {
      FileUtils.copyDirectory(srcPath.toFile(), dstPath.toFile());
    } catch (IOException e) {
      throw new BatfishException(
          "Failed to copy directory: '" + srcPath + "' to: '" + dstPath + "'", e);
    }
  }

  public static void copyFile(Path srcPath, Path dstPath) {
    try {
      FileUtils.copyFile(srcPath.toFile(), dstPath.toFile());
    } catch (IOException e) {
      throw new BatfishException("Failed to copy file: '" + srcPath + "' to: '" + dstPath + "'", e);
    }
  }

  public static void createDirectories(Path path) {
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new BatfishException(
          "Could not create directories leading up to and including '" + path + "'", e);
    }
  }

  public static ClientBuilder createHttpClientBuilder(
      boolean noSsl,
      boolean trustAllSslCerts,
      Path keystoreFile,
      String keystorePassword,
      Path truststoreFile,
      String truststorePassword) {
    ClientBuilder clientBuilder = ClientBuilder.newBuilder();
    try {
      if (!noSsl) {
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers;
        if (trustAllSslCerts) {
          trustManagers =
              new TrustManager[] {
                new X509TrustManager() {
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
              };
          clientBuilder.hostnameVerifier(new TrustAllHostNameVerifier());
        } else if (truststoreFile != null) {
          TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
          KeyStore ts = KeyStore.getInstance("JKS");
          if (truststorePassword == null) {
            throw new BatfishException("Truststore file supplied but truststore password missing");
          }
          char[] tsPass = truststorePassword.toCharArray();
          ts.load(new FileInputStream(truststoreFile.toFile()), tsPass);
          tmf.init(ts);
          trustManagers = tmf.getTrustManagers();
        } else {
          trustManagers = null;
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyManager[] keyManagers;
        if (keystoreFile != null) {
          if (keystorePassword == null) {
            throw new BatfishException("Keystore file supplied but keystore password");
          }
          char[] ksPass = keystorePassword.toCharArray();
          ks.load(new FileInputStream(keystoreFile.toFile()), ksPass);
          kmf.init(ks, ksPass);
          keyManagers = kmf.getKeyManagers();
        } else {
          keyManagers = null;
        }
        sslcontext.init(keyManagers, trustManagers, new java.security.SecureRandom());
        clientBuilder.sslContext(sslcontext);
      }
    } catch (Exception e) {
      throw new BatfishException("Error creating HTTP client builder", e);
    }
    return clientBuilder;
  }

  public static Path createTempDirectory(String prefix, FileAttribute<?>... attrs) {
    try {
      Path tempDir = Files.createTempDirectory(prefix, attrs);
      tempDir.toFile().deleteOnExit();
      return tempDir;
    } catch (IOException e) {
      throw new BatfishException("Failed to create temporary directory", e);
    }
  }

  public static Path createTempFile(String prefix, String suffix, FileAttribute<?>... attributes) {
    try {
      Path tempFile = Files.createTempFile(prefix, suffix, attributes);
      tempFile.toFile().deleteOnExit();
      return tempFile;
    } catch (IOException e) {
      throw new BatfishException("Failed to create temporary file", e);
    }
  }

  public static Path createTempFileWithContent(String prefix, String content) throws IOException {
    Path tempFilePath = Files.createTempFile(prefix, null);

    File tempFile = tempFilePath.toFile();
    tempFile.deleteOnExit();

    FileWriter writer = new FileWriter(tempFile);
    writer.write(content);
    writer.close();

    return tempFilePath;
  }

  public static void delete(Path path) {
    try {
      Files.delete(path);
    } catch (NoSuchFileException e) {
      throw new BatfishException("Cannot delete non-existent file: '" + path + "'", e);
    } catch (IOException e) {
      throw new BatfishException("Failed to delete file: " + path, e);
    }
  }

  public static void deleteDirectory(Path path) {
    try {
      FileUtils.deleteDirectory(path.toFile());
    } catch (IOException | NullPointerException e) {
      throw new BatfishException("Could not delete directory: " + path, e);
    }
  }

  public static void deleteIfExists(Path path) {
    try {
      Files.delete(path);
    } catch (NoSuchFileException e) {
      return;
    } catch (IOException e) {
      throw new BatfishException("Failed to delete file: " + path, e);
    }
  }

  public static <S extends Set<T>, T> S difference(
      Set<T> minuendSet, Set<T> subtrahendSet, Supplier<S> setConstructor) {
    S differenceSet = setConstructor.get();
    differenceSet.addAll(minuendSet);
    differenceSet.removeAll(subtrahendSet);
    return differenceSet;
  }

  public static String escape(String offendingTokenText) {
    return offendingTokenText.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r");
  }

  public static String extractBits(long l, int start, int end) {
    String s = "";
    for (int pos = end; pos >= start; pos--) {
      long mask = 1L << pos;
      long bit = l & mask;
      s += (bit != 0) ? 1 : 0;
    }
    return s;
  }

  public static Path getCanonicalPath(Path path) {
    try {
      return Paths.get(path.toFile().getCanonicalPath());
    } catch (IOException e) {
      throw new BatfishException("Could not get canonical path from: '" + path + "'");
    }
  }

  public static org.apache.commons.configuration2.Configuration getConfig(
      String overridePropertyName,
      String defaultPropertyFilename,
      Class<?> defaultPropertyLocatorClass) {
    String overriddenPath = System.getProperty(overridePropertyName);
    URL propertiesUrl;
    if (overriddenPath != null) {
      // The user provided an override, so look up that configuration instead.
      try {
        propertiesUrl = new URL(new URL("file://"), overriddenPath);
      } catch (MalformedURLException e) {
        throw new BatfishException(
            "Error treating " + overriddenPath + " as a path to a properties file", e);
      }
    } else {
      // Find the default properties file.
      propertiesUrl =
          defaultPropertyLocatorClass.getClassLoader().getResource(defaultPropertyFilename);
    }
    try {
      return new Configurations().properties(propertiesUrl);
    } catch (Exception e) {
      throw new BatfishException("Error loading configuration from " + overriddenPath, e);
    }
  }

  public static SortedSet<Path> getEntries(Path directory) {
    SortedSet<Path> entries = new TreeSet<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
      for (Path entry : stream) {
        entries.add(entry);
      }
    } catch (IOException | DirectoryIteratorException e) {
      throw new BatfishException("Error listing directory '" + directory + "'", e);
    }
    return entries;
  }

  public static String getIndentedString(String str, int indentLevel) {
    String indent = getIndentString(indentLevel);
    StringBuilder sb = new StringBuilder();
    String[] lines = str.split("\n");
    for (String line : lines) {
      sb.append(indent + line + "\n");
    }
    return sb.toString();
  }

  public static String getIndentString(int indentLevel) {

    String retString = "";

    for (int i = 0; i < indentLevel; i++) {
      retString += "  ";
    }

    return retString;
  }

  @Nullable
  public static Integer getInterfaceVlanNumber(String ifaceName) {
    String prefix = "vlan";
    String ifaceNameLower = ifaceName.toLowerCase();
    String withoutDot = ifaceNameLower.replaceAll("\\.", "");
    if (withoutDot.startsWith(prefix)) {
      String vlanStr = withoutDot.substring(prefix.length());
      if (vlanStr.length() > 0) {
        return Integer.parseInt(vlanStr);
      }
    }
    return null;
  }

  public static FileTime getLastModifiedTime(Path path) {
    try {
      return Files.getLastModifiedTime(path);
    } catch (IOException e) {
      throw new BatfishException("Failed to get last modified time for '" + path + "'");
    }
  }

  public static List<String> getMatchingStrings(String regex, Set<String> allStrings) {
    Pattern pattern;
    try {
      pattern = Pattern.compile(regex);
    } catch (PatternSyntaxException e) {
      throw new BatfishException("Supplied regex is not a valid java regex: \"" + regex + "\"", e);
    }
    return allStrings
        .stream()
        .filter(s -> pattern.matcher(s).matches())
        .collect(Collectors.toList());
  }

  public static SortedSet<Path> getSubdirectories(Path directory) {
    SortedSet<Path> subdirectories = new TreeSet<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
      for (Path entry : stream) {
        if (Files.isDirectory(entry)) {
          subdirectories.add(entry);
        }
      }
    } catch (IOException | DirectoryIteratorException e) {
      throw new BatfishException("Error listing directory '" + directory + "'", e);
    }
    return subdirectories;
  }

  public static String getTime(long millis) {
    long cs = (millis / 10) % 100;
    long s = (millis / 1000) % 60;
    long m = (millis / (1000 * 60)) % 60;
    long h = (millis / (1000 * 60 * 60)) % 24;
    String time = String.format("%02d:%02d:%02d.%02d", h, m, s, cs);
    return time;
  }

  public static <S extends Set<T>, T> S intersection(
      Set<T> set1, Set<T> set2, Supplier<S> setConstructor) {
    S intersectionSet = setConstructor.get();
    intersectionSet.addAll(set1);
    intersectionSet.retainAll(set2);
    return intersectionSet;
  }

  public static int intWidth(int n) {
    if (n == 0) {
      return 1;
    } else {
      return 32 - Integer.numberOfLeadingZeros(n);
    }
  }

  public static boolean isLoopback(String interfaceName) {
    return (interfaceName.startsWith("Loopback") || interfaceName.startsWith("lo"));
  }

  public static boolean isNullInterface(String ifaceName) {
    String lcIfaceName = ifaceName.toLowerCase();
    return lcIfaceName.startsWith("null");
  }

  public static Stream<Path> list(Path configsPath) {
    try {
      return Files.list(configsPath);
    } catch (IOException e) {
      throw new BatfishException("Could not list files in '" + configsPath + "'", e);
    }
  }

  public static String longToCommunity(Long l) {
    Long upper = l >> 16;
    Long lower = l & 0xFFFF;
    return upper + ":" + lower;
  }

  /** Returns a hex {@link String} representation of the MD5 hash digest of the input string. */
  @SuppressWarnings("deprecation") // md5 is deprecated, but used deliberately.
  public static String md5Digest(String saltedSecret) {
    return Hashing.md5().hashString(saltedSecret, StandardCharsets.UTF_8).toString();
  }

  public static void moveByCopy(Path srcPath, Path dstPath) {
    if (Files.isDirectory(srcPath)) {
      copyDirectory(srcPath, dstPath);
      deleteDirectory(srcPath);
    } else {
      copyFile(srcPath, dstPath);
      delete(srcPath);
    }
  }

  public static int nullChecker(Object a, Object b) {
    if (a == null && b == null) {
      return 0;
    } else if (a != null && b != null) {
      return 1;
    } else {
      return -1;
    }
  }

  public static void outputFileLines(Path downloadedFile, Consumer<String> outputFunction) {
    try (BufferedReader br = new BufferedReader(new FileReader(downloadedFile.toFile()))) {
      String line = null;
      while ((line = br.readLine()) != null) {
        outputFunction.accept(line + "\n");
      }
    } catch (IOException e) {
      throw new BatfishException(
          "Failed to read and output lines of file: '" + downloadedFile + "'");
    }
  }

  public static String readFile(Path file) {
    String text = null;
    try {
      text = new String(Files.readAllBytes(file), "UTF-8");
    } catch (IOException e) {
      throw new BatfishException("Failed to read file: " + file, e);
    }
    return text;
  }

  public static String readResource(String resourcePath) {
    try (InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new BatfishException("Error opening resource: '" + resourcePath + "'");
      }
      String output = IOUtils.toString(is);
      return output;
    } catch (IOException e) {
      throw new BatfishException("Could not open resource: '" + resourcePath + "'", e);
    }
  }

  public static synchronized String salt() {
    if (SALT == null) {
      SALT = readResource(BfConsts.ABSPATH_DEFAULT_SALT);
    }
    return SALT;
  }

  /** Returns a hex {@link String} representation of the SHA-256 hash digest of the input string. */
  public static String sha256Digest(String saltedSecret) {
    return Hashing.sha256().hashString(saltedSecret, StandardCharsets.UTF_8).toString();
  }

  public static void startSslServer(
      ResourceConfig resourceConfig,
      URI mgrUri,
      Path keystorePath,
      String keystorePassword,
      boolean trustAllCerts,
      Path truststorePath,
      String truststorePassword,
      Class<?> configurationLocatorClass,
      Class<?> callerClass) {
    if (keystorePath == null) {
      throw new BatfishException(
          "Cannot start SSL server without keystore. If you have none, you must disable SSL.");
    }
    // first find the file as specified.
    // if that does not work, find it relative to the binary
    Path keystoreAbsolutePath = keystorePath.toAbsolutePath();
    if (!Files.exists(keystoreAbsolutePath)) {
      String callingClass = callerClass.getCanonicalName();
      System.err.printf(
          "%s: keystore file not found at %s or %s\n",
          callingClass, keystorePath, keystoreAbsolutePath.toString());
      System.exit(1);
    }
    SSLContextConfigurator sslCon = new SSLContextConfigurator();
    sslCon.setKeyStoreFile(keystoreAbsolutePath.toString());
    sslCon.setKeyStorePass(keystorePassword);
    if (truststorePath != null) {
      if (truststorePassword == null) {
        throw new BatfishException("Truststore file supplied but truststore password missing");
      }
      sslCon.setTrustStoreFile(truststorePath.toString());
      sslCon.setTrustStorePass(truststorePassword);
    }
    boolean verifyClient = !trustAllCerts;
    GrizzlyHttpServerFactory.createHttpServer(
        mgrUri,
        resourceConfig,
        true,
        new SSLEngineConfigurator(sslCon, false, verifyClient, false));
  }

  public static <S extends Set<T>, T> S symmetricDifference(
      Set<T> set1, Set<T> set2, Supplier<S> constructor) {
    S differenceSet = constructor.get();
    differenceSet.addAll(set1);
    differenceSet.addAll(set2);
    S intersection = intersection(set1, set2, constructor);
    differenceSet.removeAll(intersection);
    return differenceSet;
  }

  public static SortedMap<Integer, String> toLineMap(String str) {
    SortedMap<Integer, String> map = new TreeMap<>();
    String[] lines = str.split("\n");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      map.put(i, line);
    }
    return map;
  }

  public static <S extends Set<T>, T> S union(
      Set<T> set1, Set<T> set2, Supplier<S> setConstructor) {
    S unionSet = setConstructor.get();
    unionSet.addAll(set1);
    unionSet.addAll(set2);
    return unionSet;
  }

  public static void writeFile(Path outputPath, String output) {
    try {
      Files.write(outputPath, output.getBytes());
    } catch (IOException e) {
      throw new BatfishException("Failed to write file: " + outputPath, e);
    }
  }

  public static void writeStreamToFile(InputStream inputStream, Path outputFile) {
    try (OutputStream fileOutputStream = new FileOutputStream(outputFile.toFile())) {
      int read = 0;
      final byte[] bytes = new byte[STREAMED_FILE_BUFFER_SIZE];
      while ((read = inputStream.read(bytes)) != -1) {
        fileOutputStream.write(bytes, 0, read);
      }
    } catch (IOException e) {
      throw new BatfishException(
          "Failed to write input stream to output file: '" + outputFile + "'");
    }
  }

}
