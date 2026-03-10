package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.BfConsts;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.WorkItem;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.datamodel.pojo.WorkStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test of {@link WorkResource}. */
@ParametersAreNonnullByDefault
public final class WorkResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull Builder getWorkTarget(
      String network, @Nullable String snapshot, @Nullable WorkType workType) {
    WebTarget target =
        target(CoordConsts.SVC_CFG_WORK_MGR2).path(CoordConstsV2.RSC_NETWORKS).path(network);
    if (snapshot != null) {
      target = target.path(CoordConstsV2.RSC_SNAPSHOTS).path(snapshot);
    }
    target = target.path(CoordConstsV2.RSC_WORK);
    if (workType != null) {
      target = target.queryParam(CoordConstsV2.QP_TYPE, workType);
    }
    return target
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  private @Nonnull Builder getWorkItemTarget(String network, String workId) {
    return target(CoordConsts.SVC_CFG_WORK_MGR2)
        .path(CoordConstsV2.RSC_NETWORKS)
        .path(network)
        .path(CoordConstsV2.RSC_WORK)
        .path(workId)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  @Before
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testQueueWorkDuplicate() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    UUID workId = UUID.randomUUID();
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    WorkItem workItem = new WorkItem(workId, network, snapshot, new HashMap<>());
    WorkItem dupWorkItem = new WorkItem(UUID.randomUUID(), network, snapshot, new HashMap<>());
    Main.getWorkMgr().queueWork(workItem);
    try (Response response =
        getWorkTarget(network, null, null)
            .post(Entity.entity(dupWorkItem, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
      String msg = response.readEntity(String.class);
      assertThat(
          msg,
          equalTo(
              String.format(
                  "Supplied WorkID '%s' is a duplicate of existing WorkID: '%s'",
                  dupWorkItem.getId(), workItem.getId())));
    }
  }

  @Test
  public void testQueueWork() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    UUID workId = UUID.randomUUID();
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    WorkItem workItem = new WorkItem(workId, network, snapshot, new HashMap<>());
    URI uri;
    try (Response response =
        getWorkTarget(network, null, null)
            .post(Entity.entity(workItem, MediaType.APPLICATION_JSON))) {
      assertThat(response.getStatus(), equalTo(CREATED.getStatusCode()));
      uri = response.getLocation();
    }
    try (Response response =
        target(uri.getPath())
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      WorkStatus retrieved = response.readEntity(WorkStatus.class);
      assertTrue(retrieved.getWorkItem().matches(workItem));
      assertThat(retrieved.getWorkItem().getId(), equalTo(workId));
    }
  }

  @Test
  public void testGetWorkStatusMissingNetwork() {
    String network = "network1";
    UUID workId = UUID.randomUUID();
    try (Response response = getWorkItemTarget(network, workId.toString()).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkStatusMissingWorkItem() {
    String network = "network1";
    UUID workId = UUID.randomUUID();
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getWorkItemTarget(network, workId.toString()).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkStatusInvalidUuid() {
    String network = "network1";
    Main.getWorkMgr().initNetwork(network, null);
    try (Response response = getWorkItemTarget(network, "@@@").get()) {
      assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkStatusWrongNetwork() throws IOException {
    String network1 = "network1";
    String network2 = "network2";
    String snapshot = "snapshot1";
    UUID workId = UUID.randomUUID();
    Main.getWorkMgr().initNetwork(network1, null);
    Main.getWorkMgr().initNetwork(network2, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network1, snapshot, ImmutableSet.of());
    WorkMgrTestUtils.initSnapshotWithTopology(network2, snapshot, ImmutableSet.of());
    WorkItem workItem = new WorkItem(workId, network1, snapshot, new HashMap<>());
    Main.getWorkMgr().queueWork(workItem);
    try (Response response = getWorkItemTarget(network2, workId.toString()).get()) {
      // work item should exist
      // Should get 404 since requested network does not match requested work ID.
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetWorkStatusPresent() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    UUID workId = UUID.randomUUID();
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    WorkItem workItem = new WorkItem(workId, network, snapshot, new HashMap<>());
    Main.getWorkMgr().queueWork(workItem);
    try (Response response = getWorkItemTarget(network, workId.toString()).get()) {
      // work item should exist
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));

      WorkStatus workStatus = response.readEntity(WorkStatus.class);

      // work ID should match
      assertThat(workStatus.getWorkItem().getId(), equalTo(workId));
    }
  }

  @Test
  public void testListIncompleteWorkNoSnapshotNoWorkType() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    UUID workId = UUID.randomUUID();
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    WorkItem workItem = new WorkItem(workId, network, snapshot, new HashMap<>());
    Main.getWorkMgr().queueWork(workItem);
    try (Response response = getWorkTarget(network, null, null).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      List<WorkStatus> workStatuses = response.readEntity(new GenericType<List<WorkStatus>>() {});

      assertThat(workStatuses, hasSize(1));
      // work ID should match
      assertThat(workStatuses.get(0).getWorkItem().getId(), equalTo(workId));
    }
  }

  @Test
  public void testListIncompleteWorkWorkTypeNoSnapshot() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of());
    UUID workId1 = UUID.randomUUID();
    WorkItem workItem1 =
        new WorkItem(
            workId1,
            network,
            snapshot,
            ImmutableMap.of(BfConsts.COMMAND_PARSE_VENDOR_SPECIFIC, ""));
    UUID workId2 = UUID.randomUUID();
    WorkItem workItem2 =
        new WorkItem(
            workId2,
            network,
            snapshot,
            new HashMap<>(ImmutableMap.of(BfConsts.COMMAND_DUMP_DP, "")));
    Main.getWorkMgr().queueWork(workItem1);
    Main.getWorkMgr().queueWork(workItem2);
    try (Response response = getWorkTarget(network, null, WorkType.DATAPLANING).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      List<WorkStatus> workStatuses = response.readEntity(new GenericType<List<WorkStatus>>() {});

      // should only contain dataplaning work item
      assertThat(workStatuses, hasSize(1));
      assertThat(workStatuses.get(0).getWorkItem().getId(), equalTo(workItem2.getId()));
    }
  }

  @Test
  public void testListIncompleteWorkSnapshotNoWorkType() throws IOException {
    String network = "network1";
    String snapshot1 = "snapshot1";
    String snapshot2 = "snapshot2";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot1, ImmutableSet.of());
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot2, ImmutableSet.of());
    UUID workId1 = UUID.randomUUID();
    WorkItem workItem1 = new WorkItem(workId1, network, snapshot1, ImmutableMap.of());
    UUID workId2 = UUID.randomUUID();
    WorkItem workItem2 = new WorkItem(workId2, network, snapshot2, ImmutableMap.of());
    Main.getWorkMgr().queueWork(workItem1);
    Main.getWorkMgr().queueWork(workItem2);
    try (Response response = getWorkTarget(network, snapshot2, null).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      List<WorkStatus> workStatuses = response.readEntity(new GenericType<List<WorkStatus>>() {});

      // should only contain snapshot2's work item
      assertThat(workStatuses, hasSize(1));
      assertThat(workStatuses.get(0).getWorkItem().getId(), equalTo(workItem2.getId()));
    }
  }
}
