package org.batfish.coordinator;

// Include the following imports to use queue APIs.
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.authorizer.*;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class Main {

   private static BatfishLogger _logger;
   private static PoolMgr _poolManager;
   private static Settings _settings;
   private static WorkMgr _workManager;
   private static Authorizer _authorizer;

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

   private static void initAuthorizer() {
      switch (_settings.getAuthorizationType()) {
         case none:
            _authorizer = new NoneAuthorizer();
            break;
         default:
            System.err.print("org.batfish.coordinator: Initialization failed. Unsupported authorizer type "
                  + _settings.getAuthorizationType());
            System.exit(1);            
      }
   }
      
   public static void main(String[] args) {
      _settings = null;
      try {
         _settings = new Settings(args);
         _logger = new BatfishLogger(_settings.getLogLevel(), false,
               _settings.getLogFile(), false);
      }
      catch (Exception e) {
         System.err.print("org.batfish.coordinator: Initialization failed: "
                     + e.getMessage());
         System.exit(1);
      }
      
      initAuthorizer();

      // start the pool manager service
      URI poolMgrUri = UriBuilder
            .fromUri("http://" + _settings.getServiceHost())
            .port(_settings.getServicePoolPort()).build();

      _logger.info("Starting pool manager at " + poolMgrUri + "\n");

      ResourceConfig rcPool = new ResourceConfig(PoolMgrService.class)
            .register(new JettisonFeature()).register(MultiPartFeature.class)
            .register(org.batfish.coordinator.CrossDomainFilter.class);

      GrizzlyHttpServerFactory.createHttpServer(poolMgrUri, rcPool);

//      URI poolMgrUri = UriBuilder
//            .fromUri("https://" + _settings.getServiceHost())
//            .port(_settings.getServicePoolPort()).build();
//
//      _logger.info("Starting pool manager at " + poolMgrUri + "\n");
//
//      ResourceConfig rcPool = new ResourceConfig(PoolMgrService.class)
//            .register(new JettisonFeature()).register(MultiPartFeature.class)
//            .register(org.batfish.coordinator.CrossDomainFilter.class);
//
//      SSLContextConfigurator sslCon = new SSLContextConfigurator();
//
//      sslCon.setKeyStoreFile(ConfigLoader.getKeystoreLocation()); // contains server keypair
//      sslCon.setKeyStorePass(ConfigLoader.getKeystorePassword());
//
//      //HttpHandler hand = ContainerFactory.createContainer(HttpHandler.class, rc);
//
//      GrizzlyHttpServerFactory.createHttpServer(poolMgrUri, rcPool, true,
//              new SSLEngineConfigurator(sslCon, false, false, false));
//
      
      // start the work manager service
      URI workMgrUri = UriBuilder
            .fromUri("http://" + _settings.getServiceHost())
            .port(_settings.getServiceWorkPort()).build();

      _logger.info("Starting work manager at " + workMgrUri + "\n");

      ResourceConfig rcWork = new ResourceConfig(WorkMgrService.class)
            .register(new JettisonFeature()).register(MultiPartFeature.class)
            .register(org.batfish.coordinator.CrossDomainFilter.class);

      GrizzlyHttpServerFactory.createHttpServer(workMgrUri, rcWork);

      // start the two managers
      _poolManager = new PoolMgr(_logger);
      _workManager = new WorkMgr(_logger);

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