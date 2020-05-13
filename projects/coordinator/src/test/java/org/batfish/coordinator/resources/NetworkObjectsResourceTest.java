package org.batfish.coordinator.resources;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.resources.NetworkObjectsResource.QP_KEY;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.version.BatfishVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class NetworkObjectsResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getTarget(String network, String key) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_OBJECTS)
        .queryParam(QP_KEY, key)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void initNetworkEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testDeleteAbsent() {
    String network = "network1";
    String key = "foo/bar";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getTarget(network, key).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteMissingNetwork() {
    String network = "network1";
    String key = "foo/bar";
    try (Response response = getTarget(network, key).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeletePresent() throws IOException {
    String network = "network1";
    String key = "foo/bar";
    Main.getWorkMgr().initNetwork(network, null);
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    Main.getWorkMgr().putNetworkObject(inputStream, network, key);
    try (Response response = getTarget(network, key).delete()) {
      // delete should succeed
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    try (Response response = getTarget(network, key).delete()) {
      // delete should fail the second time
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetAbsent() {
    String network = "network1";
    String key = "foo/bar";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getTarget(network, key).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetMissingNetwork() {
    String network = "network1";
    String key = "foo/bar";
    try (Response response = getTarget(network, key).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetPresent() throws IOException {
    String network = "network1";
    String key = "foo/bar";
    Main.getWorkMgr().initNetwork(network, null);
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    Main.getWorkMgr().putNetworkObject(inputStream, network, key);
    try (Response response = getTarget(network, key).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(String.class), equalTo(content));
    }
  }

  @Test
  public void testPutMissingNetwork() {
    String network = "network1";
    String key = "foo/bar";
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    try (Response response =
        getTarget(network, key)
            .put(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM))) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testPutNew() throws IOException {
    String network = "network1";
    String key = "foo/bar";
    Main.getWorkMgr().initNetwork(network, null);
    String content = "baz";
    try (Response response =
        getTarget(network, key).put(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM))) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      try (InputStream stream = Main.getWorkMgr().getNetworkObject(network, key)) {
        assertThat(stream, not(nullValue()));
        assertThat(IOUtils.toString(stream, UTF_8), equalTo(content));
      }
    }
  }

  @Test
  public void testPutOverwrite() throws IOException {
    String network = "network1";
    String key = "foo/bar";
    Main.getWorkMgr().initNetwork(network, null);
    String oldContent = "baz";
    String newContent = "bath";
    Main.getWorkMgr()
        .putNetworkObject(new ByteArrayInputStream(oldContent.getBytes()), network, key);
    try (Response response =
        getTarget(network, key)
            .put(Entity.entity(newContent, MediaType.APPLICATION_OCTET_STREAM))) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      try (InputStream stream = Main.getWorkMgr().getNetworkObject(network, key)) {
        assertThat(stream, not(nullValue()));
        assertThat(IOUtils.toString(stream, UTF_8), equalTo(newContent));
      }
    }
  }
}
