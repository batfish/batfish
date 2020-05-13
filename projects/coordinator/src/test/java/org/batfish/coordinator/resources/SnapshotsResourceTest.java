package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.datamodel.SnapshotMetadataEntry;
import org.batfish.version.BatfishVersion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public final class SnapshotsResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Builder getTarget(String network, boolean verbose) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .queryParam(CoordConstsV2.QP_VERBOSE, verbose)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, BatfishVersion.getVersionStatic());
  }

  @Before
  public void setup() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testListSnapshotsMissingNetwork() {
    String network = "network1";
    try (Response response = getTarget(network, false).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testListSnapshotsSuccess() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, _folder);
    try (Response response = getTarget(network, false).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(new GenericType<List<String>>() {}),
          equalTo(ImmutableList.of(snapshot)));
    }
  }

  @Test
  public void testListSnapshotsVerboseMissingNetwork() {
    String network = "network1";
    try (Response response = getTarget(network, true).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testListSnapshotsVerboseSuccess() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.uploadTestSnapshot(network, snapshot, _folder);
    try (Response response = getTarget(network, true).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(new GenericType<List<SnapshotMetadataEntry>>() {}),
          equalTo(
              ImmutableList.of(
                  new SnapshotMetadataEntry(
                      snapshot, Main.getWorkMgr().getSnapshotMetadata(network, snapshot)))));
    }
  }
}
