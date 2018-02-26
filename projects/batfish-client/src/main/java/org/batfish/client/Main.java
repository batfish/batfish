package org.batfish.client;

import java.util.LinkedList;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.batfish.client.config.Settings;

public class Main {

  public static void main(String[] args) {

    // Uncomment these lines when you want things to be captured by fiddler
    // System.setProperty("http.proxyHost", "127.0.0.1");
    // System.setProperty("https.proxyHost", "127.0.0.1");
    // System.setProperty("http.proxyPort", "8888");
    // System.setProperty("https.proxyPort", "8888");

    Settings settings = null;
    try {
      settings = new Settings(args);
    } catch (Exception e) {
      System.err.println(Main.class.getName() + ": Initialization failed:\n");
      System.err.print(ExceptionUtils.getStackTrace(e));
      System.exit(1);
    }

    Client client = new Client(settings);
    client.run(new LinkedList<String>());
  }
}
