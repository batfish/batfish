package org.batfish.coordinator;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.authorizer.Authorizer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link ApiKeyAuthenticationFilterTest}. */
public class ApiKeyAuthenticationFilterTest extends JerseyTest {

  @Path("/test")
  public static class TestService {
    @HeaderParam(CoordConsts.SVC_KEY_API_KEY)
    String _apiKey;

    @GET
    public Response get() {
      return Response.ok("GET with key: " + _apiKey).build();
    }
  }

  public class TestAuthorizer implements Authorizer {

    static final String VALID_APIKEY = "10000";

    @Override
    public void authorizeContainer(String apiKey, String containerName) {}

    @Override
    public boolean isAccessibleContainer(String apiKey, String containerName, boolean logError) {
      return false;
    }

    @Override
    public boolean isValidWorkApiKey(String apiKey) {
      return apiKey.equals(VALID_APIKEY);
    }
  }

  @Before
  public void initEnvironment() {
    Main.mainInit(new String[] {});
    Main.setAuthorizer(new TestAuthorizer());
  }

  @Override
  protected Application configure() {
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new ResourceConfig(TestService.class).register(ApiKeyAuthenticationFilter.class);
  }

  @Test
  public void testEmptyApiKey() {
    Response response = target("/test").request().header(CoordConsts.SVC_KEY_API_KEY, "").get();
    assertThat(response.getStatus(), equalTo(UNAUTHORIZED.getStatusCode()));
    String expectedMessage = "ApiKey is empty";
    assertThat(response.readEntity(String.class), equalTo(expectedMessage));
  }

  @Test
  public void testInvalidDefaultKeyWhenApiKeyIsNull() {
    Response response = target("/test").request().header(CoordConsts.SVC_KEY_API_KEY, null).get();
    assertThat(response.getStatus(), equalTo(FORBIDDEN.getStatusCode()));
    String expectMessage =
        String.format("Authorizer: %s is NOT a valid key", CoordConsts.DEFAULT_API_KEY);
    assertThat(response.readEntity(String.class), equalTo(expectMessage));
  }

  @Test
  public void testInvalidDefaultKeyWhenApiKeyIsMissing() {
    Response response = target("/test").request().get();
    assertThat(response.getStatus(), equalTo(FORBIDDEN.getStatusCode()));
    String expectMessage =
        String.format("Authorizer: %s is NOT a valid key", CoordConsts.DEFAULT_API_KEY);
    assertThat(response.readEntity(String.class), equalTo(expectMessage));
  }

  @Test
  public void testInvalidApiKey() {
    String apiKey = "10001";
    Response response = target("/test").request().header(CoordConsts.SVC_KEY_API_KEY, apiKey).get();
    assertThat(response.getStatus(), equalTo(FORBIDDEN.getStatusCode()));
    String expectMessage = String.format("Authorizer: %s is NOT a valid key", apiKey);
    assertThat(response.readEntity(String.class), equalTo(expectMessage));
  }

  @Test
  public void testValidApiKey() {
    String apiKey = TestAuthorizer.VALID_APIKEY;
    Response response = target("/test").request().header(CoordConsts.SVC_KEY_API_KEY, apiKey).get();
    assertThat(response.getStatus(), equalTo(OK.getStatusCode()));
    String expectMessage = String.format("GET with key: %s", apiKey);
    assertThat(response.readEntity(String.class), equalTo(expectMessage));
  }
}
