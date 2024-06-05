package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.batfish.coordinator.WorkMgrServiceV2.DEFAULT_NETWORK_PREFIX;
import static org.batfish.version.Versioned.UNKNOWN_VERSION;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.batfish.common.BfConsts;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.authorizer.Authorizer;
import org.batfish.coordinator.version.ApiVersion;
import org.batfish.datamodel.questions.TestQuestion;
import org.batfish.version.BatfishVersion;
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
  public void initTestEnvironment() {
    WorkMgrTestUtils.initWorkManager(_folder);
  }

  private WebTarget getContainersTarget() {
    return target(CoordConsts.SVC_CFG_WORK_MGR2).path(CoordConstsV2.RSC_NETWORKS);
  }

  @Test
  public void getContainers() {
    try (Response response =
        getContainersTarget()
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(new GenericType<List<Container>>() {}), empty());
    }

    Main.getWorkMgr().initNetwork("someContainer", null);
    try (Response response =
        getContainersTarget()
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(response.readEntity(new GenericType<List<Container>>() {}), hasSize(1));
    }
  }

  @Test
  public void redirectContainer() {
    try (Response response =
        target(CoordConsts.SVC_CFG_WORK_MGR2)
            .path(CoordConstsV2.RSC_NETWORK)
            .property(FOLLOW_REDIRECTS, false)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      assertThat(response.getStatus(), equalTo(MOVED_PERMANENTLY.getStatusCode()));
      assertThat(response.getLocation().getPath(), equalTo("/v2/networks"));
    }
  }

  @Test
  public void testGetQuestionTemplatesUnconfigured() {
    try (Response response =
        target(CoordConsts.SVC_CFG_WORK_MGR2)
            .path(CoordConstsV2.RSC_QUESTION_TEMPLATES)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      assertThat(response.getStatus(), equalTo(INTERNAL_SERVER_ERROR.getStatusCode()));
    }
  }

  @Test
  public void testGetQuestionTemplatesConfigured() {
    String templateName = "template1";
    String templateText = writeTemplateFile(templateName);
    try (Response response =
        target(CoordConsts.SVC_CFG_WORK_MGR2)
            .path(CoordConstsV2.RSC_QUESTION_TEMPLATES)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(Map.class), equalTo(ImmutableMap.of(templateName, templateText)));
    }
  }

  private @Nonnull String writeTemplateFile(String templateName) {
    Path questionTemplateDir = _folder.getRoot().toPath().resolve("templates");
    String templateText =
        String.format(
            "{\"class\":\"%s\",\"%s\":{\"%s\":\"%s\"}}",
            TestQuestion.class, BfConsts.PROP_INSTANCE, BfConsts.PROP_INSTANCE_NAME, templateName);
    Path questionTemplateFile = questionTemplateDir.resolve(templateName + ".json");
    questionTemplateDir.toFile().mkdirs();
    CommonUtil.writeFile(questionTemplateFile, templateText);
    Main.getSettings().setQuestionTemplateDirs(ImmutableList.of(questionTemplateDir));
    return templateText;
  }

  @Test
  public void testGetQuestionTemplatesConfiguredVerbose() {
    String templateName = "template1";
    String hiddenTemplateName = "__template2";
    String templateText = writeTemplateFile(templateName);
    String hiddenTemplateText = writeTemplateFile(hiddenTemplateName);
    // when not verbose, hidden template should be absent
    try (Response response =
        target(CoordConsts.SVC_CFG_WORK_MGR2)
            .path(CoordConstsV2.RSC_QUESTION_TEMPLATES)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(Map.class), equalTo(ImmutableMap.of(templateName, templateText)));
    }

    // when verbose, hidden template should be present
    try (Response response =
        target(CoordConsts.SVC_CFG_WORK_MGR2)
            .path(CoordConstsV2.RSC_QUESTION_TEMPLATES)
            .queryParam(CoordConstsV2.QP_VERBOSE, true)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          response.readEntity(Map.class),
          equalTo(
              ImmutableMap.of(templateName, templateText, hiddenTemplateName, hiddenTemplateText)));
    }
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
    try (Response resp =
        getContainersTarget()
            .path(containerName)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, myKey)
            .get()) {
      assertThat(resp.getStatus(), equalTo(OK.getStatusCode()));
      assertThat(
          resp.readEntity(Container.class),
          equalTo(Container.of(containerName, Collections.emptySortedSet())));
    }

    // Test that subsequent calls return 401 unauthorized with unknown API key
    try (Response resp =
        getContainersTarget()
            .path(containerName)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, "unknownKey")
            .get()) {
      assertThat(resp.getStatus(), equalTo(UNAUTHORIZED.getStatusCode()));
      assertThat(
          resp.readEntity(String.class), equalTo("Authorizer: 'unknownKey' is NOT a valid key"));
    }

    // Test that subsequent calls return 403 forbidden with known API key and no access
    try (Response resp =
        getContainersTarget()
            .path(containerName)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, otherKey)
            .get()) {
      assertThat(resp.getStatus(), equalTo(FORBIDDEN.getStatusCode()));
      assertThat(
          resp.readEntity(String.class),
          equalTo("network 'someContainer' is not accessible by the api key: AnotherApiKey"));
    }
  }

  @Test
  public void testGetVersion() {
    try (Response response =
        target(CoordConsts.SVC_CFG_WORK_MGR2).path(CoordConstsV2.RSC_VERSION).request().get()) {

      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      // Should get a non-unknown Batfish version
      Map<String, String> result = response.readEntity(new GenericType<Map<String, String>>() {});
      assertThat(result, hasEntry(equalTo("Batfish"), not(equalTo(UNKNOWN_VERSION))));
      assertThat(result, hasEntry(equalTo("api_version"), equalTo(ApiVersion.getVersionStatic())));
    }
  }

  /** Tests that clients that include the now-unnecessary version header are still allowed. */
  @Test
  public void testGetVersionExtraHeader() {
    try (Response response =
        target(CoordConsts.SVC_CFG_WORK_MGR2)
            .path(CoordConstsV2.RSC_VERSION)
            .request()
            .header("X-Batfish-Version", BatfishVersion.getVersionStatic())
            .get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      // Should get a non-unknown Batfish version
      Map<String, String> result = response.readEntity(new GenericType<Map<String, String>>() {});
      assertThat(result, hasEntry(equalTo("Batfish"), not(equalTo(UNKNOWN_VERSION))));
      assertThat(result, hasEntry(equalTo("api_version"), equalTo(ApiVersion.getVersionStatic())));
    }
  }

  private @Nonnull WebTarget initNetworkTarget(
      @Nullable String network, @Nullable String networkPrefix) {
    WebTarget target = target(CoordConsts.SVC_CFG_WORK_MGR2).path(CoordConstsV2.RSC_NETWORKS);
    if (network != null) {
      target = target.queryParam(CoordConstsV2.QP_NAME, network);
    }
    if (networkPrefix != null) {
      target = target.queryParam(CoordConstsV2.QP_NAME_PREFIX, networkPrefix);
    }
    return target;
  }

  @Test
  public void testInitNetworkDefaultPrefix() {
    URI outputPath;
    Container c;
    try (Response response =
        initNetworkTarget(null, null)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .post(null)) {
      assertThat(response.getStatus(), equalTo(CREATED.getStatusCode()));
      outputPath = response.getLocation();
    }
    try (Response response =
        target(outputPath.getPath())
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      c = response.readEntity(Container.class);
    }
    assertThat(c.getName(), startsWith(String.format("%s_", DEFAULT_NETWORK_PREFIX)));
    assertTrue(Main.getWorkMgr().checkNetworkExists(c.getName()));
  }

  @Test
  public void testInitNetworkAlreadyExists() {
    String network = Main.getWorkMgr().initNetwork("network", null);
    try (Response response =
        initNetworkTarget(network, null)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .post(null)) {
      assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
      String msg = response.readEntity(String.class);
      assertThat(msg, equalTo("Network 'network' already exists!"));
    }
  }

  @Test
  public void testInitNetworkQpNetworkNullNetworkPrefix() {
    String network = "net1";
    URI outputPath;
    Container c;
    try (Response response =
        initNetworkTarget(network, null)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .post(null)) {
      assertThat(response.getStatus(), equalTo(CREATED.getStatusCode()));
      outputPath = response.getLocation();
    }
    try (Response response =
        target(outputPath.getPath())
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      c = response.readEntity(Container.class);
    }
    assertThat(c.getName(), equalTo(network));
    assertTrue(Main.getWorkMgr().checkNetworkExists(c.getName()));
  }

  @Test
  public void testInitNetworkQpNetworkEmptyNetworkPrefix() {
    String network = "net1";
    URI outputPath;
    Container c;
    try (Response response =
        initNetworkTarget(network, "")
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .post(null)) {
      assertThat(response.getStatus(), equalTo(CREATED.getStatusCode()));
      outputPath = response.getLocation();
    }
    try (Response response =
        target(outputPath.getPath())
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      c = response.readEntity(Container.class);
    }
    assertThat(c.getName(), equalTo(network));
    assertTrue(Main.getWorkMgr().checkNetworkExists(c.getName()));
  }

  @Test
  public void testInitNetworkQpNetworkAndNetworkPrefix() {
    String network = "net1";
    URI outputPath;
    Container c;
    try (Response response =
        initNetworkTarget(network, "ignored")
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .post(null)) {
      assertThat(response.getStatus(), equalTo(CREATED.getStatusCode()));
      outputPath = response.getLocation();
    }
    try (Response response =
        target(outputPath.getPath())
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      c = response.readEntity(Container.class);
    }
    assertThat(c.getName(), equalTo(network));
    assertTrue(Main.getWorkMgr().checkNetworkExists(c.getName()));
  }

  @Test
  public void testInitNetworkQpNetworkPrefixNullNetwork() {
    String networkPrefix = "prefix";
    URI outputPath;
    Container c;
    try (Response response =
        initNetworkTarget(null, networkPrefix)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .post(null)) {
      assertThat(response.getStatus(), equalTo(CREATED.getStatusCode()));
      outputPath = response.getLocation();
    }
    try (Response response =
        target(outputPath.getPath())
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      c = response.readEntity(Container.class);
    }
    assertThat(c.getName(), startsWith(String.format("%s_", networkPrefix)));
    assertTrue(Main.getWorkMgr().checkNetworkExists(c.getName()));
  }

  @Test
  public void testInitNetworkQpNetworkPrefixEmptyNetwork() {
    String networkPrefix = "prefix";
    URI outputPath;
    Container c;
    try (Response response =
        initNetworkTarget("", networkPrefix)
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .post(null)) {
      assertThat(response.getStatus(), equalTo(CREATED.getStatusCode()));
      outputPath = response.getLocation();
    }
    try (Response response =
        target(outputPath.getPath())
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY)
            .get()) {
      c = response.readEntity(Container.class);
    }
    assertThat(c.getName(), startsWith(String.format("%s_", networkPrefix)));
    assertTrue(Main.getWorkMgr().checkNetworkExists(c.getName()));
  }
}
