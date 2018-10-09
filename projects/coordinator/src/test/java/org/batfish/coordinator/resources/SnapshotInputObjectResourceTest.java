package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.batfish.common.BfConsts;
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

public final class SnapshotInputObjectResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getTarget(String network, String snapshot, URI uri) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_CONTAINERS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .path(CoordConstsV2.RSC_INPUT + uri.getPath())
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testGetAbsent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initTestrigWithTopology(network, snapshot, ImmutableSet.of());
    URI uri = URI.create("file:///foo/bar");
    Response response = getTarget(network, snapshot, uri).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetMissingNetwork() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    URI uri = URI.create("file:///foo/bar");
    Response response = getTarget(network, snapshot, uri).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetMissingSnapshot() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    URI uri = URI.create("file:///foo/bar");
    Response response = getTarget(network, snapshot, uri).get();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }

  @Test
  public void testGetPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    String node = "node1";
    String content = "stuff";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, node, content, _folder);
    URI uri =
        URI.create(
            String.format(
                "file:///%s/%s/%s",
                BfConsts.RELPATH_TEST_RIG_DIR, BfConsts.RELPATH_CONFIGURATIONS_DIR, node));
    Response response = getTarget(network, snapshot, uri).get();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(String.class), equalTo(content));
  }
}
