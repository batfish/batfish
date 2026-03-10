package org.batfish.coordinator.resources;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.common.CoordConstsV2.QP_MAX_SUGGESTIONS;
import static org.batfish.common.CoordConstsV2.QP_QUERY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgrServiceV2TestBase;
import org.batfish.coordinator.WorkMgrTestUtils;
import org.batfish.coordinator.id.IdManager;
import org.batfish.datamodel.PrefixTrieMultiMap;
import org.batfish.datamodel.answers.AutocompleteResponse;
import org.batfish.datamodel.questions.Variable;
import org.batfish.datamodel.questions.Variable.Type;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test of {@link AutocompleteResource}. */
public final class AutocompleteResourceTest extends WorkMgrServiceV2TestBase {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull Builder getTarget(
      String network,
      @Nullable String snapshot,
      Variable.Type varType,
      @Nullable String query,
      @Nullable Integer maxSuggestions) {
    WebTarget t =
        target(CoordConsts.SVC_CFG_WORK_MGR2).path(CoordConstsV2.RSC_NETWORKS).path(network);
    if (snapshot != null) {
      t = t.path(CoordConstsV2.RSC_SNAPSHOTS).path(snapshot);
    }
    t = t.path(CoordConstsV2.RSC_AUTOCOMPLETE).path(varType.getName());
    if (query != null) {
      t = t.queryParam(QP_QUERY, query);
    }
    if (maxSuggestions != null) {
      t = t.queryParam(QP_MAX_SUGGESTIONS, maxSuggestions);
    }
    return t.request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  @Before
  public void initNetworkEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  @Test
  public void testGetMissingNetwork() {
    String network = "network1";
    String snapshot = "snapshot1";
    try (Response response = getTarget(network, snapshot, Type.NODE_NAME, null, null).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGetMissingSnapshot() {
    String network = "network1";
    Main.getWorkMgr().initNetwork(network, null);
    String snapshot = "snapshot1";
    try (Response response = getTarget(network, snapshot, Type.NODE_NAME, null, null).get()) {
      assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));
    }
  }

  @Test
  public void testGet() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of("node1"));

    CompletionMetadata completionMetadata =
        new CompletionMetadata(
            ImmutableSet.of(),
            ImmutableSet.of(),
            new PrefixTrieMultiMap<>(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(
                "node1",
                new NodeCompletionMetadata(null),
                "node2",
                new NodeCompletionMetadata(null)),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of());
    IdManager idm = Main.getWorkMgr().getIdManager();
    NetworkId networkId = idm.getNetworkId(network).get();
    SnapshotId snapshotId = idm.getSnapshotId(snapshot, networkId).get();
    Main.getWorkMgr()
        .getStorage()
        .storeCompletionMetadata(completionMetadata, networkId, snapshotId);

    // empty query without explicit suggestion limit
    // should get 2 results
    try (Response response = getTarget(network, snapshot, Type.NODE_NAME, null, null).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      AutocompleteResponse aresp = response.readEntity(AutocompleteResponse.class);
      assertThat(aresp.getSuggestions(), hasSize(2));
    }
    // empty query with limit of 1 suggestion
    // should get 1 result
    try (Response response = getTarget(network, snapshot, Type.NODE_NAME, null, 1).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      AutocompleteResponse aresp = response.readEntity(AutocompleteResponse.class);
      assertThat(aresp.getSuggestions(), hasSize(1));
    }
    // query 'node2' without explicit suggestion limit
    // should get 1 result
    try (Response response = getTarget(network, snapshot, Type.NODE_NAME, "node2", null).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      AutocompleteResponse aresp = response.readEntity(AutocompleteResponse.class);
      assertThat(aresp.getSuggestions(), hasSize(1));
    }
  }

  @Test
  public void testGetNoSnapshotButNeedsSnapshot() throws IOException {
    String network = "network1";
    String snapshot = "snapshot1";
    Main.getWorkMgr().initNetwork(network, null);
    WorkMgrTestUtils.initSnapshotWithTopology(network, snapshot, ImmutableSet.of("node1"));

    CompletionMetadata completionMetadata =
        new CompletionMetadata(
            ImmutableSet.of(),
            ImmutableSet.of(),
            new PrefixTrieMultiMap<>(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableMap.of(
                "node1",
                new NodeCompletionMetadata(null),
                "node2",
                new NodeCompletionMetadata(null)),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of());
    IdManager idm = Main.getWorkMgr().getIdManager();
    NetworkId networkId = idm.getNetworkId(network).get();
    SnapshotId snapshotId = idm.getSnapshotId(snapshot, networkId).get();
    Main.getWorkMgr()
        .getStorage()
        .storeCompletionMetadata(completionMetadata, networkId, snapshotId);

    // should get 0 results since a snapshot is needed for NODE_NAME
    try (Response response = getTarget(network, null, Type.NODE_NAME, null, null).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      AutocompleteResponse aresp = response.readEntity(AutocompleteResponse.class);
      assertThat(aresp.getSuggestions(), empty());
    }
  }

  @Test
  public void testGetNoSnapshot() {
    String network = "network1";
    Main.getWorkMgr().initNetwork(network, null);

    try (Response response =
        getTarget(network, null, Type.BGP_PROCESS_PROPERTY_SPEC, null, null).get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      AutocompleteResponse aresp = response.readEntity(AutocompleteResponse.class);
      // should get results
      assertThat(aresp.getSuggestions(), not(empty()));
    }
  }
}
