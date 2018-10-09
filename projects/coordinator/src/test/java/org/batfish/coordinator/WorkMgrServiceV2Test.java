package org.batfish.coordinator;

import static javax.ws.rs.client.Invocation.Builder;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.Version;
import org.batfish.coordinator.authorizer.Authorizer;
import org.batfish.coordinator.resources.ForkSnapshotBean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class WorkMgrServiceV2Test extends WorkMgrServiceV2TestBase {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initContainerEnvironment() throws Exception {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private WebTarget getContainersTarget() {
    return target(CoordConsts.SVC_CFG_WORK_MGR2).path(CoordConstsV2.RSC_NETWORKS);
  }

  private Builder getSnapshotTarget(String network, String snapshot) {
    return getContainersTarget()
        .path(network)
        .path(CoordConstsV2.RSC_SNAPSHOTS)
        .path(snapshot)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  @Test
  public void forkSnapshot() throws IOException {
    String networkName = "network";
    String snapshotName = "snapshot";
    String baseSnapshotName = "baseSnapshot";

    ForkSnapshotBean forkSnapshotBean =
        new ForkSnapshotBean(
            baseSnapshotName, ImmutableList.of(), ImmutableList.of(), ImmutableList.of("node1"));

    Response response =
        getSnapshotTarget(networkName, snapshotName).put(Entity.json(forkSnapshotBean));
    // Confirm missing network causes not found
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));

    Main.getWorkMgr().initNetwork(networkName, null);
    response = getSnapshotTarget(networkName, snapshotName).put(Entity.json(forkSnapshotBean));
    // Confirm missing snapshot causes not found
    assertThat(response.getStatus(), equalTo(NOT_FOUND.getStatusCode()));

    WorkMgrTestUtils.initSnapshot(networkName, baseSnapshotName);
    response = getSnapshotTarget(networkName, snapshotName).put(Entity.json(forkSnapshotBean));
    // Confirm forking existing snapshot is successful
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));

    response = getSnapshotTarget(networkName, snapshotName).put(Entity.json(forkSnapshotBean));
    // Confirm duplicate snapshot name fails with bad request
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void getContainers() {
    Response response =
        getContainersTarget()
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<List<Container>>() {}), empty());

    Main.getWorkMgr().initNetwork("someContainer", null);
    response =
        getContainersTarget()
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(new GenericType<List<Container>>() {}), hasSize(1));
  }

  @Test
  public void redirectContainer() {
    Response response =
        target(CoordConsts.SVC_CFG_WORK_MGR2)
            .path(CoordConstsV2.RSC_NETWORK)
            .property(FOLLOW_REDIRECTS, false)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get();
    assertThat(response.getStatus(), equalTo(MOVED_PERMANENTLY.getStatusCode()));
    assertThat(response.getLocation().getPath(), equalTo("/v2/networks"));
  }

  /** Test that the ApiKey is extracted from the correct header */
  @Test
  public void apiKeyValidationAndCorrectReturnCodes() {
    // Set up by making a call to authorize container
    String containerName = "someContainer";
    String otherContainerName = "anotherContainer";
    String myKey = "ApiKey";
    String otherKey = "AnotherApiKey";
    Authorizer auth = new MapAuthorizer();
    Main.setAuthorizer(auth);
    auth.authorizeContainer(myKey, containerName);
    auth.authorizeContainer(otherKey, otherContainerName);
    Main.getWorkMgr().initNetwork(containerName, null);

    // Test that subsequent calls return 200 with correct API key
    Response resp =
        getContainersTarget()
            .path(containerName)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, myKey)
            .get();
    assertThat(resp.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(
        resp.readEntity(Container.class),
        equalTo(Container.of(containerName, Collections.emptySortedSet())));

    // Test that subsequent calls return 401 unauthorized with unknown API key
    resp =
        getContainersTarget()
            .path(containerName)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, "unknownKey")
            .get();
    assertThat(resp.getStatus(), equalTo(UNAUTHORIZED.getStatusCode()));
    assertThat(
        resp.readEntity(String.class), equalTo("Authorizer: 'unknownKey' is NOT a valid key"));

    // Test that subsequent calls return 403 forbidden with known API key and no access
    resp =
        getContainersTarget()
            .path(containerName)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION, Version.getVersion())
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, otherKey)
            .get();
    assertThat(resp.getStatus(), equalTo(FORBIDDEN.getStatusCode()));
    assertThat(
        resp.readEntity(String.class),
        equalTo("network 'someContainer' is not accessible by the api key: AnotherApiKey"));
  }
}
