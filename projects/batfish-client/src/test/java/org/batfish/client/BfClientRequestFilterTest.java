package org.batfish.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link org.batfish.client.BfClientRequestFilter}. */
public class BfClientRequestFilterTest extends JerseyTest {

  @Path("/test")
  public static class TestService {
    @GET
    public Response get(@Context HttpHeaders headers) {
      if (headers
              .getRequestHeader(CoordConsts.SVC_KEY_API_KEY)
              .get(0)
              .equals(CoordConsts.DEFAULT_API_KEY)
          && headers
              .getRequestHeader(CoordConsts.SVC_KEY_VERSION)
              .get(0)
              .equals(Version.getVersion())) {
        return Response.ok().build();
      }
      return Response.serverError().build();
    }
  }

  @Override
  protected Application configure() {
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new ResourceConfig(TestService.class);
  }

  @Test
  public void testFilter() {
    Client client = ClientBuilder.newClient();
    Response response = client.target(getBaseUri()).path("/test").request().get();
    Assert.assertEquals(500, response.getStatus());
    client.register(BfClientRequestFilter.class);
    response = client.target(getBaseUri()).path("/test").request().get();
    Assert.assertEquals(200, response.getStatus());
  }
}
