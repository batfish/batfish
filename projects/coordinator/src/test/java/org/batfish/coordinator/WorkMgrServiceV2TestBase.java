package org.batfish.coordinator;

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

  // Grizzly test container logger is identified by a private inner class for some reason
  private static Logger GRIZZLY_TEST_CONTAINER_LOGGER =
      Logger.getLogger(
          String.format("%s$GrizzlyTestContainer", GrizzlyTestContainerFactory.class.getName()));

  // must be done statically to maintain reference
  private static Logger HTTP_SERVER_LOGGER = Logger.getLogger(HttpServer.class.getName());

  // must be done statically to maintain reference
  private static Logger NETWORK_LISTENER_LOGGER = Logger.getLogger(NetworkListener.class.getName());

  /** Test log-level for HTTP server library components. */
  private static final Level TEST_LOG_LEVEL = Level.OFF;

  public WorkMgrServiceV2TestBase() {
    GRIZZLY_TEST_CONTAINER_LOGGER.setLevel(TEST_LOG_LEVEL);
    HTTP_SERVER_LOGGER.setLevel(TEST_LOG_LEVEL);
    NETWORK_LISTENER_LOGGER.setLevel(TEST_LOG_LEVEL);
  }

  @Override
  public Application configure() {
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new ResourceConfig(WorkMgrServiceV2.class)
        .register(ServiceObjectMapper.class)
        .register(ExceptionMapper.class)
        .register(JacksonFeature.class)
        .register(ApiKeyAuthenticationFilter.class)
        .register(VersionCompatibilityFilter.class)
        .register(CrossDomainFilter.class);
  }

  @Override
  protected void configureClient(ClientConfig config) {
    config.register(ServiceObjectMapper.class);
  }
}
