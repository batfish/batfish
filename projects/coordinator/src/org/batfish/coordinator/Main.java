package org.batfish.coordinator;

// Include the following imports to use queue APIs.
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.*;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Main {

   private static Logger _logger;
   private static PoolMgr _poolManager;
   private static Settings _settings;
   private static WorkMgr _workManager;

   private static String LOG_FILE = null;

   private static final String LOG_FILE_KEY = "LOG_FILE";
   public static final String MAIN_LOGGER = "MainLogger";
   private static final String ROUTING_KEY_NAME = "ROUTINGKEY";

   private static final String SLAVE_ROUTING_KEY_VALUE = "slave";

   public static PoolMgr getPoolMgr() {
      return _poolManager;
   }

   public static Settings getSettings() {
      return _settings;
   }

   public static WorkMgr getWorkMgr() {
      return _workManager;
   }

   public static Logger initializeLogger() {
      if (LOG_FILE != null) {
         ThreadContext.put(ROUTING_KEY_NAME, SLAVE_ROUTING_KEY_VALUE);
         ThreadContext.put(LOG_FILE_KEY, LOG_FILE);
      }
      return LogManager.getLogger(MAIN_LOGGER);
   }

   public static void main(String[] args) {
      _logger = LogManager.getLogger(MAIN_LOGGER);
      _settings = null;
      try {
         _settings = new Settings(args);
      }
      catch (ParseException e) {
         _logger
               .fatal("org.batfish.coordinator: Parsing command-line failed. Reason: "
                     + e.getMessage());
         System.exit(1);
      }

      LOG_FILE = _settings.getLogFile();
      initializeLogger();

      // start the pool manager service
      URI poolMgrUri = UriBuilder
            .fromUri("http://" + _settings.getServiceHost())
            .port(_settings.getServicePoolPort()).build();

      _logger.info("Starting pool manager at " + poolMgrUri + "\n");

      ResourceConfig rcPool = new ResourceConfig(PoolMgrService.class)
            .register(new JettisonFeature()).register(MultiPartFeature.class)
            .register(org.batfish.coordinator.CrossDomainFilter.class);

      GrizzlyHttpServerFactory.createHttpServer(poolMgrUri, rcPool);

      // start the work manager service
      URI workMgrUri = UriBuilder
            .fromUri("http://" + _settings.getServiceHost())
            .port(_settings.getServiceWorkPort()).build();

      _logger.info("Starting work manager at " + workMgrUri + "\n");

      ResourceConfig rcWork = new ResourceConfig(WorkMgrService.class)
            .register(new JettisonFeature()).register(MultiPartFeature.class)
            .register(org.batfish.coordinator.CrossDomainFilter.class);

      // rcPool.getProperties().put(
      // "com.sun.jersey.spi.container.ContainerResponseFilters",
      // "org.batfish.coordinator.CrossDomainFilter"
      // );

      // ResourceConfig rcWork = new ResourceConfig(WorkMgrService.class);
      // rcWork.getProperties().put(
      // "com.sun.jersey.spi.container.ContainerResponseFilters",
      // "org.batfish.coordinator.CrossDomainFilter");
      //
      // rcWork.register(new JettisonFeature())
      // .register(MultiPartFeature.class);

      GrizzlyHttpServerFactory.createHttpServer(workMgrUri, rcWork);

      // start the two managers
      _poolManager = new PoolMgr();
      _workManager = new WorkMgr();

      String initialWorker = _settings.getInitialWorker();
      if (initialWorker != null && !initialWorker.isEmpty()) {

         // workaround for cygwin replacing ':' with '?'
         initialWorker = initialWorker.replace('?', ':');

         _logger.info("Adding initial worker " + initialWorker + "\n");
         _poolManager.addToPool(initialWorker);
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