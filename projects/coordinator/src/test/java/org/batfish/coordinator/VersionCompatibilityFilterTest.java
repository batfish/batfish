package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

/** Tests for {@link VersionCompatibilityFilter}. */
public class VersionCompatibilityFilterTest extends JerseyTest {

  @Path("/test")
  public static class TestService {
    @HeaderParam(CoordConsts.SVC_KEY_VERSION)
    String _clientVersion;

    @GET
    public Response get() {
      return Response.ok("GET with version: " + _clientVersion).build();
    }
  }

  @Override
  protected Application configure() {
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new ResourceConfig(TestService.class).register(VersionCompatibilityFilter.class);
  }

  @Test
  public void testMissingVersion() {
    Response response = target("/test").request().get();
    assertThat(response.getStatus(), equalTo(PRECONDITION_FAILED.getStatusCode()));
    String expectedMessage = "Version is missing or empty";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testEmptyVersion() {
    Response response = target("/test").request().header(CoordConsts.SVC_KEY_VERSION, "").get();
    assertThat(response.getStatus(), equalTo(PRECONDITION_FAILED.getStatusCode()));
    String expectedMessage = "Version is missing or empty";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testNullVersion() {
    Response response = target("/test").request().header(CoordConsts.SVC_KEY_VERSION, null).get();
    assertThat(response.getStatus(), equalTo(PRECONDITION_FAILED.getStatusCode()));
    String expectedMessage = "Version is missing or empty";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testBadVersion() throws Exception {
    String version = "0.0.1";
    Response response =
        target("/test").request().header(CoordConsts.SVC_KEY_VERSION, version).get();
    assertThat(response.getStatus(), equalTo(PRECONDITION_FAILED.getStatusCode()));
    String expectedMessage = String.format("Illegal version '%s' for Client", version);
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testValidVersion() throws Exception {
    String version = Version.getVersion();
    Response response =
        target("/test").request().header(CoordConsts.SVC_KEY_VERSION, version).get();
    String expectedMessage = String.format("GET with version: %s", version);
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }
}
