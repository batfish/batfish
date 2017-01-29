package org.batfish.coordinator;

// Include the following imports to use queue APIs.
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.authorizer.*;
import org.batfish.coordinator.config.ConfigurationLocator;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Main {

   private static Authorizer _authorizer;
   private static BatfishLogger _logger;
   private static PoolMgr _poolManager;
   private static Settings _settings;
   private static WorkMgr _workManager;

   static Logger httpServerLogger = Logger.getLogger(
         org.glassfish.grizzly.http.server.HttpServer.class.getName());
   static Logger networkListenerLogger = Logger
         .getLogger("org.glassfish.grizzly.http.server.NetworkListener");

   public static Authorizer getAuthorizer() {
      return _authorizer;
   }

   public static BatfishLogger getLogger() {
      return _logger;
   }

   public static PoolMgr getPoolMgr() {
      return _poolManager;
   }

   public static Settings getSettings() {
      return _settings;
   }

   public static WorkMgr getWorkMgr() {
      return _workManager;
   }

   private static void initAuthorizer() throws Exception {
      switch (_settings.getAuthorizationType()) {
      case none:
         _authorizer = new NoneAuthorizer();
         break;
      case file:
         _authorizer = new FileAuthorizer();
         break;
      case database:
         _authorizer = new DbAuthorizer();
         break;
      default:
         System.err.print(
               "org.batfish.coordinator: Initialization failed. Unsupported authorizer type "
                     + _settings.getAuthorizationType());
         System.exit(1);
      }
   }

   private static void initPoolManager() {

      ResourceConfig rcPool = new ResourceConfig(PoolMgrService.class)
            .register(new JettisonFeature()).register(MultiPartFeature.class)
            .register(org.batfish.coordinator.CrossDomainFilter.class);

      if (!_settings.getUseSsl()) {
         URI poolMgrUri = UriBuilder
               .fromUri("http://" + _settings.getServiceHost())
               .port(_settings.getServicePoolPort()).build();

         _logger.info("Starting pool manager at " + poolMgrUri + "\n");

         GrizzlyHttpServerFactory.createHttpServer(poolMgrUri, rcPool);
      }
      else {
         URI poolMgrUri = UriBuilder
               .fromUri("https://" + _settings.getServiceHost())
               .port(_settings.getServicePoolPort()).build();

         _logger.info("Starting pool manager at " + poolMgrUri + "\n");

         // first find the file as specified.
         // if that does not work, find it relative to the binary
         File keystoreFile = new File(_settings.getSslKeystoreFilename());
         if (!keystoreFile.exists()) {
            keystoreFile = Paths
                  .get(CommonUtil.getJarOrClassDir(ConfigurationLocator.class)
                        .getAbsolutePath(), _settings.getSslKeystoreFilename())
                  .toFile();
         }

         if (!keystoreFile.exists()) {
            System.err.printf(
                  "org.batfish.coordinator: keystore file not found at %s or %s\n",
                  _settings.getSslKeystoreFilename(),
                  keystoreFile.getAbsolutePath());
            System.exit(1);
         }
         /* Uncomment below to enable fine glassfish/grizzly ssl logging */
         // Logger l =
         // Logger.getLogger("org.glassfish.grizzly.ssl.SSLContextConfigurator");
         // l.setLevel(Level.FINE);
         // l.setUseParentHandlers(false);
         // ConsoleHandler ch = new ConsoleHandler();
         // ch.setLevel(Level.ALL);
         // l.addHandler(ch);
         SSLContextConfigurator sslCon = new SSLContextConfigurator();
         sslCon.setKeyStoreFile(keystoreFile.getAbsolutePath());
         sslCon.setKeyStorePass(_settings.getSslKeystorePassword());

         GrizzlyHttpServerFactory.createHttpServer(poolMgrUri, rcPool, true,
               new SSLEngineConfigurator(sslCon, false, false, false));
      }

      _poolManager = new PoolMgr(_logger);
      _poolManager.startPoolManager();

   }

   private static void initWorkManager() {
      ResourceConfig rcWork = new ResourceConfig(WorkMgrService.class)
            .register(new JettisonFeature()).register(MultiPartFeature.class)
            .register(org.batfish.coordinator.CrossDomainFilter.class);

      if (!_settings.getUseSsl()) {
         URI workMgrUri = UriBuilder
               .fromUri("http://" + _settings.getServiceHost())
               .port(_settings.getServiceWorkPort()).build();

         _logger.info("Starting work manager at " + workMgrUri + "\n");

         GrizzlyHttpServerFactory.createHttpServer(workMgrUri, rcWork);
      }
      else {
         URI workMgrUri = UriBuilder
               .fromUri("https://" + _settings.getServiceHost())
               .port(_settings.getServiceWorkPort()).build();

         _logger.info("Starting work manager at " + workMgrUri + "\n");

         // first find the file as specified.
         // if that does not work, find it relative to the binary
         File keystoreFile = new File(_settings.getSslKeystoreFilename());
         if (!keystoreFile.exists()) {
            keystoreFile = Paths
                  .get(CommonUtil.getJarOrClassDir(ConfigurationLocator.class)
                        .getAbsolutePath(), _settings.getSslKeystoreFilename())
                  .toFile();
         }

         if (!keystoreFile.exists()) {
            System.err.printf(
                  "org.batfish.coordinator: keystore file not found at %s or %s\n",
                  _settings.getSslKeystoreFilename(),
                  keystoreFile.getAbsolutePath());
            System.exit(1);
         }

         SSLContextConfigurator sslCon = new SSLContextConfigurator();
         sslCon.setKeyStoreFile(keystoreFile.getAbsolutePath());
         sslCon.setKeyStorePass(_settings.getSslKeystorePassword());

         GrizzlyHttpServerFactory.createHttpServer(workMgrUri, rcWork, true,
               new SSLEngineConfigurator(sslCon, false, false, false));
      }

      _workManager = new WorkMgr(_logger);
      _workManager.startWorkManager();
   }

   public static void main(String[] args) {
      mainInit(args);
      _logger = new BatfishLogger(_settings.getLogLevel(), false,
            _settings.getLogFile(), false, true);
      mainRun();
   }

   public static void main(String[] args, BatfishLogger logger) {
      mainInit(args);
      _logger = logger;
      mainRun();
   }

   public static void mainInit(String[] args) {
      _settings = null;
      try {
         _settings = new Settings(args);
         networkListenerLogger.setLevel(Level.WARNING);
         httpServerLogger.setLevel(Level.WARNING);
      }
      catch (Exception e) {
         System.err.print("org.batfish.coordinator: Initialization failed: "
               + e.getMessage());
         System.exit(1);
      }
   }

   private static void mainRun() {
      try {
         initAuthorizer();
         initPoolManager();
         initWorkManager();
      }
      catch (Exception e) {
         System.err.println(
               "org.batfish.coordinator: Initialization of a helper failed: "
                     + e.getMessage());
         System.exit(1);
      }

      // sleep indefinitely, in 10 minute chunks
      try {
         while (true) {
            Thread.sleep(10 * 60 * 1000); // 10 minutes
            _logger.info("Still alive .... waiting for work to show up\n");
         }
      }
      catch (Exception ex) {
         String stackTrace = ExceptionUtils.getFullStackTrace(ex);
         System.err.println(stackTrace);
      }
   }
}
