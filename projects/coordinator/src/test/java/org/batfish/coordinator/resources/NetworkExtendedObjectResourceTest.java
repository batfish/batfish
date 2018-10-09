package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.Version;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class NetworkExtendedObjectResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getTarget(String network, URI uri) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(network)
        .path(CoordConstsV2.RSC_EXTENDED + uri.getPath())
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testDeleteAbsent() throws IOException {
    String network = "network1";
    URI uri = URI.create("file:///foo/bar");
    Main.getWorkMgr().initNetwork(network, null);
    Response response = getTarget(network, uri).delete();
    response = getTarget(network, uri).delete();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testDeleteMissingNetwork() throws IOException {
    String network = "network1";
    URI uri = URI.create("file:///foo/bar");
    Response response = getTarget(network, uri).delete();
    response = getTarget(network, uri).delete();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testDeletePresent() throws IOException {
    String network = "network1";
    URI uri = URI.create("file:///foo/bar");
    Main.getWorkMgr().initNetwork(network, null);
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    Main.getWorkMgr().putNetworkExtendedObject(inputStream, network, uri);
    Response response = getTarget(network, uri).delete();

    // delete should succeed
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));

    response = getTarget(network, uri).delete();

    // delete should fail the second time
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetAbsent() throws IOException {
    String network = "network1";
    URI uri = URI.create("file:///foo/bar");
    Main.getWorkMgr().initNetwork(network, null);
    Response response = getTarget(network, uri).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetMissingNetwork() throws IOException {
    String network = "network1";
    URI uri = URI.create("file:///foo/bar");
    Response response = getTarget(network, uri).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetPresent() throws IOException {
    String network = "network1";
    URI uri = URI.create("file:///foo/bar");
    Main.getWorkMgr().initNetwork(network, null);
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    Main.getWorkMgr().putNetworkExtendedObject(inputStream, network, uri);
    Response response = getTarget(network, uri).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(String.class), equalTo(content));
  }

  @Test
  public void testPutMissingNetwork() throws IOException {
    String network = "network1";
    URI uri = URI.create("file:///foo/bar");
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    Response response =
        getTarget(network, uri).put(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testPutNew() throws IOException {
    String network = "network1";
    URI uri = URI.create("file:///foo/bar");
    Main.getWorkMgr().initNetwork(network, null);
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    Response response =
        getTarget(network, uri).put(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        IOUtils.toString(Main.getWorkMgr().getNetworkExtendedObject(network, uri)),
        equalTo(content));
  }

  @Test
  public void testPutOverwrite() throws IOException {
    String network = "network1";
    URI uri = URI.create("file:///foo/bar");
    Main.getWorkMgr().initNetwork(network, null);
    String oldContent = "baz";
    String newContent = "bath";
    InputStream oldContentInputStream = new ByteArrayInputStream(oldContent.getBytes());
    InputStream newContentInputStream = new ByteArrayInputStream(newContent.getBytes());
    Main.getWorkMgr().putNetworkExtendedObject(oldContentInputStream, network, uri);
    Response response =
        getTarget(network, uri)
            .put(Entity.entity(newContentInputStream, MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        IOUtils.toString(Main.getWorkMgr().getNetworkExtendedObject(network, uri)),
        equalTo(newContent));
  }
}
