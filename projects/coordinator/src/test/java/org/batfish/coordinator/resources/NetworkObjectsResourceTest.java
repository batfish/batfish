package org.batfish.coordinator.resources;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.resources.NetworkObjectsResource.QP_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class NetworkObjectsResourceTest extends WorkMgrServiceV2TestBase {

  @TempDir public File _folder;

  private Builder getTarget(String network, String key) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_OBJECTS)
        .queryParam(QP_KEY, key)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  @BeforeEach
  void initNetworkEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  void testDeleteAbsent() {
    String network = "network1";
    String key = "foo/bar";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getTarget(network, key).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  void testDeleteMissingNetwork() {
    String network = "network1";
    String key = "foo/bar";
    try (Response response = getTarget(network, key).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  void testDeletePresent() throws IOException {
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
  void testGetAbsent() {
    String network = "network1";
    String key = "foo/bar";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getTarget(network, key).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  void testGetMissingNetwork() {
    String network = "network1";
    String key = "foo/bar";
    try (Response response = getTarget(network, key).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  void testGetPresent() throws IOException {
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
  void testPutMissingNetwork() {
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
  void testPutNew() throws IOException {
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
  void testPutOverwrite() throws IOException {
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
