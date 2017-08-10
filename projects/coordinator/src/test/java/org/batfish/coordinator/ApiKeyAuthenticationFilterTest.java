package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.config.Settings;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link ApiKeyAuthenticationFilterTest} */
public class ApiKeyAuthenticationFilterTest extends JerseyTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initEnvironment() throws Exception {
    BatfishLogger logger = new BatfishLogger("debug", false);
    Settings settings = new Settings(new String[] {});
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    Main.setLogger(logger);
    Main.initAuthorizer();
    Main.setWorkMgr(new WorkMgr(settings, logger));
  }

  @Override
  protected Application configure() {
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new ResourceConfig(WorkMgrServiceV2.class)
        .register(ExceptionMapper.class)
        .register(JacksonFeature.class)
        .register(MultiPartFeature.class)
        .register(CrossDomainFilter.class)
        .register(ApiKeyAuthenticationFilter.class);
  }

  @Test
  public void testEmptyApiKey() throws Exception {
    Response response =
        target("/v2/containers").request().header(CoordConsts.SVC_KEY_API_KEY, "").get();
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    String expectedMessage = "apikey is missing or empty";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testNullApiKey() throws Exception {
    Response response =
        target("/v2/containers").request().header(CoordConsts.SVC_KEY_API_KEY, null).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }

  @Test
  public void testDefaultApiKey() throws Exception {
    Response response = target("/v2/containers").request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }

  @Test
  public void testValidApiKey() throws Exception {
    Response response =
        target("/v2/containers").request().header(CoordConsts.SVC_KEY_API_KEY, "10000").get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }
}
