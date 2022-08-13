package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test of {@link ApiVersionService}. */
public final class ApiVersionServiceTest extends MainServiceTestBase {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Before
  public void initTestEnvironment() {
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {"-containerslocation", _folder.getRoot().toString()});
    Main.setLogger(logger);
    Main.initAuthorizer();
  }

  private @Nonnull Builder getTarget() {
    return target(CoordConsts.SVC_CFG_API_VERSION)
        .request()
        .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, CoordConsts.DEFAULT_API_KEY);
  }

  @Test
  public void testGet() {
    try (Response response = getTarget().get()) {
      assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
      ApiVersions versions = response.readEntity(ApiVersions.class);
      assertThat(versions, equalTo(ApiVersions.instance()));
    }
  }
}
