package org.batfish.coordinator.resources;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.resources.SnapshotObjectsResource.QP_KEY;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.storage.StoredObjectMetadata;
import org.batfish.version.BatfishVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class SnapshotObjectsResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getTarget(String network, String snapshot, String key) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_OBJECTS)
        .queryParam(QP_KEY, key)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  private Builder listKeysTarget(String network, String snapshot) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_OBJECTS)
        .path(CoordConstsV2.RSC_LIST)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testDeleteAbsent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    String key = "foo/bar";
    try (Response response = getTarget(network, snapshot, key).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    String key = "foo/bar";
    try (Response response = getTarget(network, snapshot, key).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeleteMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    String key = "foo/bar";
    try (Response response = getTarget(network, snapshot, key).delete()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testDeletePresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    String key = "foo/bar";
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    Main.getWorkMgr().putSnapshotExtendedObject(inputStream, network, snapshot, key);
    try (Response response = getTarget(network, snapshot, key).delete()) {
      // delete should succeed
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    }

    try (Response response = getTarget(network, snapshot, key).delete()) {
      // delete should fail the second time
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetAbsent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    String key = "foo/bar";
    try (Response response = getTarget(network, snapshot, key).get()) {

      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    String key = "foo/bar";
    try (Response response = getTarget(network, snapshot, key).get()) {

      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    String key = "foo/bar";
    try (Response response = getTarget(network, snapshot, key).get()) {

      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    String key = "foo/bar";
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    Main.getWorkMgr().putSnapshotExtendedObject(inputStream, network, snapshot, key);
    try (Response response = getTarget(network, snapshot, key).get()) {

      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(String.class), equalTo(content));
    }
  }

  @Test
  public void testPutMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    String key = "foo/bar";
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    try (Response response =
        getTarget(network, snapshot, key)
            .put(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM))) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testPutMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    String key = "foo/bar";
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    try (Response response =
        getTarget(network, snapshot, key)
            .put(Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM))) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testPutNew() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    String key = "foo/bar";
    String content = "baz";
    try (Response response =
        getTarget(network, snapshot, key)
            .put(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM))) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      try (InputStream stream = Main.getWorkMgr().getSnapshotObject(network, snapshot, key)) {
        assertThat(stream, not(nullValue()));
        assertThat(IOUtils.toString(stream, UTF_8), equalTo(content));
      }
    }
  }

  @Test
  public void testPutOverwrite() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    String key = "foo/bar";
    String oldContent = "baz";
    String newContent = "bath";
    InputStream oldContentInputStream = new ByteArrayInputStream(oldContent.getBytes());
    Main.getWorkMgr().putSnapshotExtendedObject(oldContentInputStream, network, snapshot, key);
    try (Response response =
        getTarget(network, snapshot, key)
            .put(Entity.entity(newContent, MediaType.APPLICATION_OCTET_STREAM))) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      try (InputStream stream = Main.getWorkMgr().getSnapshotObject(network, snapshot, key)) {
        assertThat(stream, not(nullValue()));
        assertThat(IOUtils.toString(stream, UTF_8), equalTo(newContent));
      }
    }
  }

  @Test
  public void testListKeys() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    String key = "foo/bar";
    String content = "baz";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    Main.getWorkMgr().putSnapshotExtendedObject(inputStream, network, snapshot, key);

    try (Response response = listKeysTarget(network, snapshot).get()) {

      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));

      assertThat(
          response.readEntity(new GenericType<List<StoredObjectMetadata>>() {}),
          equalTo(ImmutableList.of(new StoredObjectMetadata(key, content.getBytes().length))));
    }
  }

  @Test
  public void testListKeysMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";

    try (Response response = listKeysTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testListKeysMissingSnapshot() {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = listKeysTarget(network, snapshot).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }
}
