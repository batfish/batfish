package org.batfish.coordinator;

// Include the following imports to use queue APIs.
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Main {

   public static final String MAIN_LOGGER = "MainLogger";

   private static Settings _settings;
   private static PoolMgr _poolManager;
   private static WorkMgr _workManager;
   
   public static void main(String[] args) {

      _settings = null;
      try {
         _settings = new Settings(args);
      }
      catch (ParseException e) {
         System.err.println("org.batfish.coordinator: Parsing command-line failed. Reason: "
               + e.getMessage());
         System.exit(1);
      }

      //start the pool manager service
      URI poolMgrUri = UriBuilder.fromUri("http://" + _settings.getServiceHost())
            .port(_settings.getServicePoolPort()).build();

      System.out
            .println(String.format("Starting pool manager at %s\n", poolMgrUri));

      ResourceConfig rcPool = new ResourceConfig(PoolMgrService.class)
            .register(new JettisonFeature())
            .register(MultiPartFeature.class);

      GrizzlyHttpServerFactory.createHttpServer(poolMgrUri, rcPool);

      //start the work manager service
      URI workMgrUri = UriBuilder.fromUri("http://" + _settings.getServiceHost())
            .port(_settings.getServiceWorkPort()).build();

      System.out
            .println(String.format("Starting work manager at %s\n", workMgrUri));

      ResourceConfig rcWork = new ResourceConfig(WorkMgrService.class)
            .register(new JettisonFeature())
            .register(MultiPartFeature.class);

      GrizzlyHttpServerFactory.createHttpServer(workMgrUri, rcWork);

      //start the two managers
      _poolManager = new PoolMgr();
      _workManager = new WorkMgr();
      
      //sleep indefinitely, in 10 minute chunks
      try {
         while (true) {
            Thread.sleep(10 * 60 * 1000);  //10 minutes
            System.out.println("Still alive ....");
         }
      }
      catch (Exception ex) {
         String stackTrace = ExceptionUtils.getFullStackTrace(ex);
         System.err.println(stackTrace);
      }
   }
   
   public static PoolMgr getPoolMgr() {
      return _poolManager;
   }
   
   public static WorkMgr getWorkMgr() {
      return _workManager;
   }
   
   public static Settings getSettings() {
      return _settings;
   }
}