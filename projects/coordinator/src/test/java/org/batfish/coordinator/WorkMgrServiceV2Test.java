package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static javax.ws.rs.core.Response.Status.OK;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
import org.batfish.coordinator.config.Settings;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
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
    Main.initAuthorizer();
    Main.setLogger(logger);
    Main.setWorkMgr(new WorkMgr(settings, logger));
  }

  @Override
  protected Application configure() {
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new ResourceConfig(WorkMgrServiceV2.class)
        .register(ExceptionMapper.class)
        .register(JacksonFeature.class)
        .register(MultiPartFeature.class)
        .register(CrossDomainFilter.class);
  }

  @Test
  public void getContainers() throws Exception {
    Response response = target("/v2/containers").request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<List<Container>>() {}), empty());

    Main.getWorkMgr().initContainer("someContainer", null);
    response = target().path("/v2/containers").request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<List<Container>>() {}), hasSize(1));
  }

  @Test
  public void redirectContainer() throws Exception {
    Response response = target("/v2/container").property(FOLLOW_REDIRECTS, false).request().get();
    assertThat(response.getStatus(), equalTo(MOVED_PERMANENTLY.getStatusCode()));
    assertThat(response.getLocation().getPath(), equalTo("/v2/containers"));
  }

  public void getInfo() throws Exception {
    Response response = target().path("/v2/info").request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    JSONObject object = new JSONObject();
    object.put("Service name", "Batfish coordinator");
    object.put(CoordConsts.SVC_KEY_VERSION, Version.getVersion());
    object.put("APIs", "Enter ../application.wadl (relative to your URL) to see supported methods");
    assertThat(response.readEntity(String.class), equalTo(object.toString()));
  }

  @Test
  public void getStatus() throws Exception {
    Response response = target().path("/v2/status").request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    JSONObject object = Main.getWorkMgr().getStatusJson();
    object.put("service-version", Version.getVersion());
    assertThat(response.readEntity(String.class), equalTo(object.toString()));
  }

  @Test
  public void getSubresource() throws Exception {
    String containerName = "someContainer";
    String testrigsUri =
        target().path("/v2/container").path(containerName).path("/testrigs").getUri().toString();
    Container expected = Container.makeContainer(containerName, testrigsUri);
    Main.getWorkMgr().initContainer(containerName, null);
    Response response = target().path("/v2/container").path(containerName).request().get();
    if (response.getStatus() == 500) {
      System.err.println("Content is: " + response.readEntity(String.class));
    }
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<Container>() {}), equalTo(expected));
  }

  @Test
  public void testModifyContainer() {
    String containerName = "someContainer";
    // init container
    Response response =
        target()
            .path("/v2/container")
            .path(containerName)
            .request()
            .post(Entity.entity(String.class, MediaType.APPLICATION_JSON));
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    String expectedMessage = "Container '" + containerName + "' created";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
    // Post existing container
    response =
        target()
            .path("/v2/container")
            .path(containerName)
            .request()
            .post(Entity.entity(String.class, MediaType.APPLICATION_JSON));
    assertThat(response.getStatus(), equalTo(INTERNAL_SERVER_ERROR.getStatusCode()));
    expectedMessage = "Container '" + containerName + "' already exists!";
    assertThat(response.readEntity(String.class), containsString(expectedMessage));

    // Delete existing container
    response = target().path("/v2/container").path(containerName).request().delete();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    expectedMessage = "Container '" + containerName + "' deleted";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void deleteNonExistingContainer() {
    Response response =
        target().path("/v2/container").path("nonExistingContainer").request().delete();
    assertThat(response.getStatus(), equalTo(INTERNAL_SERVER_ERROR.getStatusCode()));
    String expectedMessage = "Container 'nonExistingContainer' does not exist";
    assertThat(response.readEntity(String.class), containsString(expectedMessage));
  }

  @Test
  public void getEmptyTestrigs() throws Exception {
    String containerName = "someContainer";
    Main.getWorkMgr().initContainer(containerName, null);
    Response response =
        target().path("/v2/container").path(containerName).path("/testrigs").request().get();
    if (response.getStatus() == 500) {
      System.err.println("Content is: " + response.readEntity(String.class));
    }
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(String.class), equalTo("[]"));
  }

  @Test
  public void redirectTestrigs() throws Exception {
    String containerName = "someContainer";
    Main.getWorkMgr().initContainer(containerName, null);
    Response response =
        target()
            .property(FOLLOW_REDIRECTS, false)
            .path("/v2/container/someContainer/testrig")
            .request()
            .get();
    assertThat(response.getStatus(), equalTo(MOVED_PERMANENTLY.getStatusCode()));
    assertThat(response.getLocation().getPath(), equalTo("/v2/container/someContainer/testrigs"));
  }

}
