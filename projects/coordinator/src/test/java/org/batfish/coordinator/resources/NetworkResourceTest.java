package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.coordinator.WorkMgrTestUtils.uploadTestSnapshot;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.batfish.common.Container;
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

@ParametersAreNonnullByDefault
public final class NetworkResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainer() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private Builder getNetworkTarget(String network) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion());
  }

  private Builder getForkSnapshotTarget(String network) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS + ":" + CoordConstsV2.RSC_FORK)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  @Test
  public void testForkSnapshot() throws IOException {
    String networkName = "network";
    String snapshotName = "snapshot";
    String baseSnapshotName = "baseSnapshot";

    ForkSnapshotBean forkSnapshotBean =
        new ForkSnapshotBean(
            baseSnapshotName,
            snapshotName,
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableList.of("node1"),
            null);

    Response response = getForkSnapshotTarget(networkName).post(Entity.json(forkSnapshotBean));
    // Confirm missing network causes not found
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));

    Main.getWorkMgr().initNetwork(networkName, null);
    response = getForkSnapshotTarget(networkName).post(Entity.json(forkSnapshotBean));
    // Confirm missing snapshot causes not found
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));

    uploadTestSnapshot(networkName, baseSnapshotName, _folder);
    response = getForkSnapshotTarget(networkName).post(Entity.json(forkSnapshotBean));
    // Confirm forking existing snapshot is successful
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));

    response = getForkSnapshotTarget(networkName).post(Entity.json(forkSnapshotBean));
    // Confirm duplicate snapshot name fails with bad request
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void testForkSnapshotBadRequest() throws IOException {
    String networkName = "network";
    String snapshotName = "snapshot";
    String baseSnapshotName = "baseSnapshot";
    Main.getWorkMgr().initNetwork(networkName, null);

    ForkSnapshotBean forkSnapshotBeanNoName =
        new ForkSnapshotBean(
            baseSnapshotName,
            null,
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableList.of(),
            null);
    ForkSnapshotBean forkSnapshotBeanNoBaseName =
        new ForkSnapshotBean(
            null, snapshotName, ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), null);

    Response response =
        getForkSnapshotTarget(networkName).post(Entity.json(forkSnapshotBeanNoName));
    // Confirm no snapshot name fails with bad request
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));

    response = getForkSnapshotTarget(networkName).post(Entity.json(forkSnapshotBeanNoBaseName));
    // Confirm no base snapshot name fails with bad request
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void testGetContainer() {
    String containerName = "someContainer";
    Main.getWorkMgr().initNetwork(containerName, null);
    Response response = getNetworkTarget(containerName).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        response.readEntity(new GenericType<Container>() {}).getName(), equalTo(containerName));
  }

  @Test
  public void testDeleteNetwork() {
    String network = "network1";
    Main.getWorkMgr().initNetwork(network, null);
    Response response = getNetworkTarget(network).delete();

    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }

  @Test
  public void testDeleteNetworkMissingNetwork() {
    String network = "network1";
    Response response = getNetworkTarget(network).delete();

    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
  }
}
