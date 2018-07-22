package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.batfish.common.CoordConstsV2.HTTP_HEADER_BATFISH_VERSION;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.batfish.common.Version;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

/** Tests for {@link VersionCompatibilityFilter}. */
public class VersionCompatibilityFilterTest extends JerseyTest {

  @Path("/test")
  public static class TestService {
    @GET
    public Response get() {
      return Response.ok().build();
    }
  }

  @Path("/crash")
  public static class CrashService {
    public CrashService() {
      throw new IllegalStateException("CrashService should never be instantiated");
    }

    @GET
    public Response get() {
      return Response.ok().build();
    }
  }

  @Override
  protected Application configure() {
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new ResourceConfig(TestService.class, CrashService.class)
        .register(VersionCompatibilityFilter.class);
  }

  @Test
  public void testMissingVersionPreMatch() {
    Response response = target("/crash").request().get();
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    String expectMessage =
        String.format(
            "HTTP header %s should contain a client version", HTTP_HEADER_BATFISH_VERSION);
    assertThat(response.readEntity(String.class), equalTo(expectMessage));
  }

  @Test
  public void testEmptyVersionPreMatch() {
    Response response = target("/crash").request().header(HTTP_HEADER_BATFISH_VERSION, "").get();
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    String expectMessage =
        String.format(
            "HTTP header %s should contain a client version", HTTP_HEADER_BATFISH_VERSION);
    assertThat(response.readEntity(String.class), equalTo(expectMessage));
  }

  @Test
  public void testBadVersionPreMatch() {
    Response response =
        target("/crash").request().header(HTTP_HEADER_BATFISH_VERSION, "1.0.1").get();
    assertThat(response.getStatus(), equalTo(BAD_REQUEST.getStatusCode()));
    assertThat(
        response.readEntity(String.class),
        equalTo(
            String.format(
                "Client version '1.0.1' is not compatible with server version '%s'",
                Version.getVersion())));
  }

  @Test
  public void testValidVersion() {
    Response response =
        target("/test").request().header(HTTP_HEADER_BATFISH_VERSION, Version.getVersion()).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
  }
}
