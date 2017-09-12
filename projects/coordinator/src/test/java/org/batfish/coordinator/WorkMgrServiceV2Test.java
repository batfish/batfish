package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.List;
import java.util.TreeSet;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.config.Settings;
import org.batfish.datamodel.pojo.CreateContainerRequest;
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

  private static final String BASE_CONTAINER_PATH =
      Paths.get(CoordConsts.SVC_CFG_WORK_MGR2, CoordConsts.SVC_KEY_CONTAINERS).toString();

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
  public void getContainers() {
    Response response = target(BASE_CONTAINER_PATH).request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<List<Container>>() {}), empty());

    Main.getWorkMgr().initContainer("some Container", null);
    response = target(BASE_CONTAINER_PATH).request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<List<Container>>() {}), hasSize(1));
  }

  @Test
  public void redirectContainer() {
    String baseContainerPath =
        Paths.get(CoordConsts.SVC_CFG_WORK_MGR2, CoordConsts.SVC_KEY_CONTAINER_NAME).toString();
    Response response = target(baseContainerPath).property(FOLLOW_REDIRECTS, false).request().get();
    assertThat(response.getStatus(), equalTo(MOVED_PERMANENTLY.getStatusCode()));
    assertThat(response.getLocation().getPath(), equalTo(BASE_CONTAINER_PATH));
  }

  @Test
  public void testCreateContainerWithBody() {
    // init container
    String baseContainerPath =
        Paths.get(CoordConsts.SVC_CFG_WORK_MGR2, CoordConsts.SVC_KEY_CONTAINER_NAME).toString();
    Response response =
        target()
            .path(baseContainerPath)
            .request()
            .post(
                Entity.entity(
                    new CreateContainerRequest("container", true), MediaType.APPLICATION_JSON));
    assertThat(response.getStatus(), equalTo(CREATED.getStatusCode()));
  }

  @Test
  public void getSubresource() {
    String containerName = "someContainer";
    Container expected = Container.of(containerName, new TreeSet<>());
    Main.getWorkMgr().initContainer(containerName, null);
    Response response = target().path(BASE_CONTAINER_PATH).path(containerName).request().get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<Container>() {}), equalTo(expected));
  }

  @Test
  public void testModifyContainer() {
    String containerName = "someContainer";
    // init container
    Response response =
        target()
            .path(BASE_CONTAINER_PATH)
            .path(containerName)
            .request()
            .post(Entity.entity(String.class, MediaType.APPLICATION_JSON));
    assertThat(response.getStatus(), equalTo(CREATED.getStatusCode()));
    assertThat(
        response.getLocation(),
        equalTo(target().path(BASE_CONTAINER_PATH).path(containerName).getUri()));
    // post existing container
    response =
        target()
            .path(BASE_CONTAINER_PATH)
            .path(containerName)
            .request()
            .post(Entity.entity(String.class, MediaType.APPLICATION_JSON));
    assertThat(response.getStatus(), equalTo(INTERNAL_SERVER_ERROR.getStatusCode()));
    String expectedMessage = String.format("Container '%s' already exists!", containerName);
    assertThat(response.readEntity(String.class), containsString(expectedMessage));
    // delete existing container
    response = target().path(BASE_CONTAINER_PATH).path(containerName).request().delete();
    assertThat(response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
  }

  @Test
  public void deleteNonExistingContainer() {
    Response response =
        target().path(BASE_CONTAINER_PATH).path("nonExistingContainer").request().delete();
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void getEmptyTestrigs() {
    String containerName = "someContainer";
    Main.getWorkMgr().initContainer(containerName, null);
    Response response =
        target()
            .path(BASE_CONTAINER_PATH)
            .path(containerName)
            .path(CoordConsts.SVC_KEY_TESTRIGS)
            .request()
            .get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(String.class), equalTo("[]"));
  }

  @Test
  public void redirectTestrig() {
    String containerName = "someContainer";
    Main.getWorkMgr().initContainer(containerName, null);
    Response response =
        target()
            .path(BASE_CONTAINER_PATH)
            .path(containerName)
            .path(CoordConsts.SVC_KEY_TESTRIG)
            .property(FOLLOW_REDIRECTS, false)
            .request()
            .get();
    assertThat(response.getStatus(), equalTo(MOVED_PERMANENTLY.getStatusCode()));
    assertThat(
        response.getLocation().getPath(),
        equalTo(
            Paths.get(BASE_CONTAINER_PATH, containerName, CoordConsts.SVC_KEY_TESTRIGS)
                .toString()));
  }
}
