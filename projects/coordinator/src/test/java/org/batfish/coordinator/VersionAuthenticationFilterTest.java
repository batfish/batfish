package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
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

/** Tests for {@link VersionAuthenticationFilter} */
public class VersionAuthenticationFilterTest extends JerseyTest {

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
        .register(VersionAuthenticationFilter.class);
  }

  @Test
  public void testMissingVersion() throws Exception {
    Response response = target("/v2/containers").request().get();
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    String expectedMessage = "version is missing or empty";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testEmptyVersion() throws Exception {
    Response response =
        target("/v2/containers").request().header(CoordConsts.SVC_KEY_VERSION, "").get();
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    String expectedMessage = "version is missing or empty";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testNullVersion() throws Exception {
    Response response =
        target("/v2/containers").request().header(CoordConsts.SVC_KEY_VERSION, null).get();
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    String expectedMessage = "version is missing or empty";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testBadVersion() throws Exception {
    String version = "0.0.1";
    Response response =
        target("/v2/containers").request().header(CoordConsts.SVC_KEY_VERSION, version).get();
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    String expectedMessage = String.format("Illegal version '%s' for Client", version);
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testValidVersion() throws Exception {
    String version = Version.getVersion();
    Response response =
        target("/v2/containers").request().header(CoordConsts.SVC_KEY_VERSION, version).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }
}
