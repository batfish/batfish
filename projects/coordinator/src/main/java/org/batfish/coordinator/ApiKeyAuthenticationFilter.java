package org.batfish.coordinator;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Strings;
import java.security.AccessControlException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.batfish.common.CoordConsts;

/**
 * This filter verify the access permissions for a user based on apiKey provided in request header.
 */
@Provider
public class ApiKeyAuthenticationFilter implements ContainerRequestFilter {
  @Override
  public void filter(ContainerRequestContext requestContext) {
    String apiKey =
        firstNonNull(
            requestContext.getHeaderString(CoordConsts.SVC_KEY_API_KEY),
            CoordConsts.DEFAULT_API_KEY);
    try {
      if (Strings.isNullOrEmpty(apiKey)) {
        throw new IllegalArgumentException("apikey is missing or empty");
      }

      checkApiKeyValidity(apiKey);

    } catch (Exception e) {
      requestContext.abortWith(
          Response.status(Status.BAD_REQUEST)
              .entity(e.getMessage())
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }

  private void checkApiKeyValidity(String apiKey) throws Exception {
    if (!Main.getAuthorizer().isValidWorkApiKey(apiKey)) {
      throw new AccessControlException("Invalid API key: " + apiKey);
    }
  }
}
