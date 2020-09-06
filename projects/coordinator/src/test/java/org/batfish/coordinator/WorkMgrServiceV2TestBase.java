package org.batfish.coordinator;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;

public class WorkMgrServiceV2TestBase extends JerseyTest {

  // Must be done statically to maintain references, or else 3rd-party code will not use our
  // settings. Do not move getLogger calls out of static initialization.
  private static final Logger[] LOGGERS =
      new Logger[] {
        Logger.getLogger(
            String.format("%s$GrizzlyTestContainer", GrizzlyTestContainerFactory.class.getName())),
        Logger.getLogger(HttpServer.class.getName()),
        Logger.getLogger(NetworkListener.class.getName())
      };

  public WorkMgrServiceV2TestBase() {
    Arrays.stream(LOGGERS).forEach(logger -> logger.setLevel(Level.OFF));
  }

  @Override
  public Application configure() {
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new ResourceConfig(WorkMgrServiceV2.class)
        .register(ServiceObjectMapper.class)
        .register(ExceptionMapper.class)
        .register(JacksonFeature.class)
        .register(ApiKeyAuthenticationFilter.class)
        .register(VersionCompatibilityFilter.class);
  }

  @Override
  protected void configureClient(ClientConfig config) {
    config.register(ServiceObjectMapper.class);
  }
}
