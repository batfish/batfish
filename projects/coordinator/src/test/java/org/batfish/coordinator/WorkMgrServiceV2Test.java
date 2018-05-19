package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.authorizer.Authorizer;
import org.batfish.coordinator.config.Settings;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class WorkMgrServiceV2Test extends JerseyTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
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
        .register(CrossDomainFilter.class);
  }

  private WebTarget getContainersTarget() {
    return target(CoordConsts.SVC_CFG_WORK_MGR2).path(CoordConsts.SVC_KEY_CONTAINERS);
  }

  @Test
  public void getContainers() {
    Response response = getContainersTarget().request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<List<Container>>() {}), empty());

    WorkMgr.initContainer("someContainer", null);
    response = getContainersTarget().request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<List<Container>>() {}), hasSize(1));
  }

  @Test
  public void redirectContainer() {
    Response response =
        target(CoordConsts.SVC_CFG_WORK_MGR2)
            .path(CoordConsts.SVC_KEY_CONTAINER_NAME)
            .property(FOLLOW_REDIRECTS, false)
            .request()
            .get();
    assertThat(response.getStatus(), equalTo(MOVED_PERMANENTLY.getStatusCode()));
    assertThat(response.getLocation().getPath(), equalTo("/v2/containers"));
  }

  @Test
  public void testGetContainer() {
    String containerName = "someContainer";
    WorkMgr.initContainer(containerName, null);
    Response response = getContainersTarget().path(containerName).request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(new GenericType<Container>() {}).getName(), equalTo(containerName));
  }

  @Test
  public void testDeleteContainer() {
    String containerName = "someContainer";
    WorkMgr.initContainer(containerName, null);
    Response response = getContainersTarget().path(containerName).request().delete();
    assertThat(response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
  }

  @Test
  public void deleteNonExistingContainer() {
    Response response = getContainersTarget().path("nonExistingContainer").request().delete();
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  /** Test that the ApiKey is extracted from the correct header */
  @Test
  public void correctApiKeyHeader() {
    // Set up by making a call to authorize container
    String containerName = "someContainer";
    String myKey = "ApiKey";
    Authorizer auth = new MapAuthorizer();
    Main.setAuthorizer(auth);
    auth.authorizeContainer(myKey, containerName);
    WorkMgr.initContainer(containerName, null);

    // Test that subsequent calls return 200 with correct API key
    Response resp =
        getContainersTarget()
            .path(containerName)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, myKey)
            .get();
    assertThat(resp.getStatus(), equalTo(OK.getStatusCode()));

    // Test that subsequent calls return 403 forbidden with wrong API key
    resp =
        getContainersTarget()
            .path(containerName)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, "wrongKey")
            .get();
    assertThat(resp.getStatus(), equalTo(FORBIDDEN.getStatusCode()));
  }
}
